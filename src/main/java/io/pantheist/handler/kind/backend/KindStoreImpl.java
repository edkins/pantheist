package io.pantheist.handler.kind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.pantheist.common.shared.model.CommonSharedModelFactory;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.filesystem.backend.JsonSnapshot;
import io.pantheist.handler.kind.model.Kind;
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

	@Inject
	private KindStoreImpl(
			final FilesystemStore filesystem,
			final SqlService sqlService,
			final SqlModelFactory sqlFactory,
			final CommonSharedModelFactory sharedFactory)
	{
		this.filesystem = checkNotNull(filesystem);
		this.sqlService = checkNotNull(sqlService);
		this.sqlFactory = checkNotNull(sqlFactory);
		this.sharedFactory = checkNotNull(sharedFactory);
	}

	@Override
	public Possible<Void> putKind(final String kindId, final Kind kind)
	{
		final JsonSnapshot<Kind> snapshot = filesystem.jsonSnapshot(path(kindId), Kind.class);

		snapshot.exists(); // don't care
		snapshot.write(kind);
		return View.noContent();
	}

	private FsPath path(final String kindId)
	{
		return kindDir().segment(kindId);
	}

	private FsPath kindDir()
	{
		return filesystem.systemBucket().segment("kind");
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

	private boolean hasParent(final Kind kind, final String parentId)
	{
		if (kind.schema().identification() == null || !kind.schema().identification().has("parentKind"))
		{
			return false;
		}
		return parentId.equals(kind.schema().identification().get("parentKind").textValue());
	}

	@Override
	public AntiIterator<Kind> listChildKinds(final String parentId)
	{
		return listAllKinds()
				.filter(k -> hasParent(k, parentId));
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
}
