package io.pantheist.handler.kind.plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.kind.model.KindComputed;
import io.pantheist.handler.kind.model.KindHandler;
import io.pantheist.plugin.annotations.ChangeHook;
import io.pantheist.plugin.annotations.PantheistPlugin;
import io.pantheist.plugin.interfaces.AlteredSnapshot;

@PantheistPlugin
public final class KindPlugin
{
	private static final Logger LOGGER = LogManager.getLogger(KindPlugin.class);

	@Inject
	public KindPlugin()
	{

	}

	@ChangeHook
	public void changeHook(final AlteredSnapshot snapshot)
	{
		final Map<String, Kind> kinds = new HashMap<>();
		snapshot.listFilesAndDirectories(snapshot.baseDir())
				.filter(snapshot::safeIsFile)
				.forEach(path -> {
					final Kind kind = snapshot.readJson(path, Kind.class);

					// If the kindId specified in the file is wrong, use the filename as the canonical kindId.
					final String kindId = kindIdFromPath(snapshot, path);
					kind.setKindId(kindId);
					kinds.put(kindId, kind);
				});

		kinds.values().forEach(k -> compute(k, kinds));

		for (final Kind k : kinds.values())
		{
			final FsPath path = path(snapshot, k.kindId());
			snapshot.writeJson(path, k);
		}
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
			LOGGER.warn("kind cycle: {}", alreadyVisited);
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

			// Add hooks to beginning so that parent hooks get added first
			computed.addHooksToBeginning(kind.specified().hooks());
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
		// We need to go all the way to the top to make sure all the hooks get added
		if (parentId.isPresent())
		{
			if (!kinds.containsKey(parentId.get()))
			{
				LOGGER.warn("Missing parent kind: {} from {}", parentId.get(), kindId);
				return;
			}
			computeRecursive(computed, kinds.get(parentId.get()), alreadyVisited, kinds);
		}
	}

	private FsPath path(final AlteredSnapshot snapshot, final String kindId)
	{
		return snapshot.baseDir().segment(kindId);
	}

	private String kindIdFromPath(final AlteredSnapshot snapshot, final FsPath path)
	{
		return path.segmentsRelativeTo(snapshot.baseDir()).failIfMultiple().get();
	}
}
