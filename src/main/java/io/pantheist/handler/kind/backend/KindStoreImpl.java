package io.pantheist.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.common.shared.model.CommonSharedModelFactory;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.filesystem.backend.JsonSnapshot;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.kind.model.KindComputed;
import io.pantheist.handler.kind.model.KindHandler;
import io.pantheist.handler.kind.model.KindProperty;
import io.pantheist.handler.sql.backend.SqlService;
import io.pantheist.handler.sql.model.SqlModelFactory;
import io.pantheist.handler.sql.model.SqlProperty;

final class KindStoreImpl implements KindStore
{
	private final FilesystemStore filesystem;
	private final SqlService sqlService;
	private final SqlModelFactory sqlFactory;
	private final CommonSharedModelFactory sharedFactory;
	private final ObjectMapper objectMapper;

	@Inject
	private KindStoreImpl(
			final FilesystemStore filesystem,
			final SqlService sqlService,
			final SqlModelFactory sqlFactory,
			final CommonSharedModelFactory sharedFactory,
			final ObjectMapper objectMapper)
	{
		this.filesystem = checkNotNull(filesystem);
		this.sqlService = checkNotNull(sqlService);
		this.sqlFactory = checkNotNull(sqlFactory);
		this.sharedFactory = checkNotNull(sharedFactory);
		this.objectMapper = checkNotNull(objectMapper);
	}

	@Override
	public Possible<Void> putKind(final String kindId, final Kind kind, final boolean failIfExists)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		final FsPath path = path(kindId);

		if (snapshot.isFile(path) && failIfExists)
		{
			return FailureReason.ALREADY_EXISTS.happened();
		}
		// Recompute not only this kind, but all the others as well
		// since some of the information stored in "computed" has to do with how
		// one kind relates to another, or is inherited from parent kinds.
		recomputeUsingSnapshot(snapshot, Optional.of(kind));
		return View.noContent();
	}

	private FsPath path(final String kindId)
	{
		return kindDir().segment(kindId);
	}

	String kindIdFromPath(final FsPath path)
	{
		return path.segmentsRelativeTo(kindDir()).failIfMultiple().get();
	}

	private FsPath kindDir()
	{
		return filesystem.projectBucket().segment("kind");
	}

	@Override
	public Optional<Kind> getKind(final String kindId)
	{
		final JsonSnapshot<Kind> snapshot = filesystem.jsonSnapshot(path(kindId), Kind.class);

		if (snapshot.exists())
		{
			return Optional.of(snapshot.read());
		}
		else
		{
			return Optional.empty();
		}
	}

	@Override
	public AntiIterator<Kind> listAllKinds()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot
				.listFilesAndDirectories(kindDir())
				.filter(snapshot::safeIsFile)
				.map(path -> snapshot.readJson(path, Kind.class));
	}

	private SqlProperty toSqlProperty(final Entry<String, KindProperty> e)
	{
		return sqlFactory.property(
				e.getKey(),
				e.getValue().typeInfo(sharedFactory),
				e.getValue().isIdentifier());
	}

	@Override
	public void registerKindsInSql()
	{
		listAllKinds().forEach(k -> {
			if (k.shouldRegisterInSql())
			{
				final List<SqlProperty> properties = k.schema()
						.properties()
						.entrySet()
						.stream()
						.map(this::toSqlProperty)
						.collect(Collectors.toList());
				sqlService.createTable(k.kindId(), properties);
			}
		});
	}

	@Override
	public AntiIterator<SqlProperty> listSqlPropertiesOfKind(final String kindId)
	{
		final Optional<Kind> kind = getKind(kindId);
		if (!kind.isPresent() || !kind.get().shouldRegisterInSql())
		{
			return AntiIt.empty();
		}
		return AntiIt.from(kind.get().schema()
				.properties()
				.entrySet())
				.map(this::toSqlProperty);
	}

	/**
	 * Figure out all the computable properties of this kind, and store them in its
	 * (mutable) computed field.
	 *
	 * Note that all kinds should end up with a handler, so if a null handler is returned
	 * it means there was an error with the kind setup.
	 *
	 * Note that the computed properties of kinds will not be used here, to allow us to
	 * compute the properties of everything at the same time.
	 *
	 * @param kind the kind we're interested in
	 * @param kinds a map of all kinds, including this one, correctly filed under their kindId.
	 */
	private void compute(final Kind kind, final Map<String, Kind> kinds)
	{
		kind.computed().clear();
		computeRecursive(kind.computed(), kind, new HashSet<>(), kinds);

		kind.computed().setChildKindIds(
				kinds.values()
						.stream()
						.filter(k -> k.hasParent(kind.kindId()))
						.map(Kind::kindId)
						.collect(Collectors.toList()));
	}

	/**
	 * @param computed the computed info we're ultimately interested in, and are mutably annotating with extra facts
	 * @param kind the kind we're recursively pointing to, which is working its way up the chain of parent kinds
	 * @param alreadyVisited a mutable set of kindId's that we've already visited, only used to guard against cycles
	 * @param kinds a map (indexed by kindId) of all kinds known to the system (incidentally including this one)
	 */
	private void computeRecursive(
			final KindComputed computed,
			final Kind kind,
			final Set<String> alreadyVisited,
			final Map<String, Kind> kinds)
	{
		final String kindId = kind.kindId();
		final Optional<String> parentId = kind.parent();

		if (alreadyVisited.contains(kindId))
		{
			// Kind cycle. Shouldn't happen if you've set them up right.
			return;
		}
		alreadyVisited.add(kindId);

		if (kind.specified() != null)
		{
			if (computed.mimeType() == null
					&& kind.specified().mimeType() != null)
			{
				computed.setMimeType(kind.specified().mimeType());
			}
		}

		if (computed.handler() == null)
		{
			if (kindId.equals("file"))
			{
				computed.setHandler(KindHandler.file);
			}
			else if (!parentId.isPresent())
			{
				/*
				 * Previously, built-in kinds would not derive from one of the known
				 * base handlers. This behaviour is deprecated and will be removed.
				 */
				computed.setHandler(KindHandler.legacy);
			}
		}

		/*
		 * This also relates to legacy code
		 */
		if (!computed.isEntityKind())
		{
			if (kind.hasParent("file") || kindId.equals("java-file"))
			{
				computed.setEntityKind();
			}
		}

		// Anything that's still missing, look it up in the parent kind.
		if (parentId.isPresent() && kinds.containsKey(parentId.get()))
		{
			computeRecursive(computed, kinds.get(parentId.get()), alreadyVisited, kinds);
		}
	}

	@Override
	public void recomputeKinds()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		recomputeUsingSnapshot(snapshot, Optional.empty());
	}

	private void recomputeUsingSnapshot(final FilesystemSnapshot snapshot, final Optional<Kind> extra)
	{
		final Map<String, Kind> kinds = new HashMap<>();
		snapshot.listFilesAndDirectories(kindDir())
				.filter(snapshot::safeIsFile)
				.forEach(path -> {
					final Kind kind = snapshot.readJson(path, Kind.class);

					// If the kindId specified in the file is wrong, use the filename as the canonical kindId.
					final String kindId = kindIdFromPath(path);
					kind.setKindId(kindId);
					kinds.put(kindId, kind);
				});

		// Store the extra kind if one was given, possibly overwriting the one that was there.
		if (extra.isPresent())
		{
			kinds.put(extra.get().kindId(), extra.get());
		}

		kinds.values().forEach(k -> compute(k, kinds));

		snapshot.write(map -> {
			for (final Kind k : kinds.values())
			{
				final File file = map.get(path(k.kindId()));
				objectMapper.writeValue(file, k);
			}
		});
	}
}
