package io.pantheist.handler.filekind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.common.api.model.CommonApiModelFactory;
import io.pantheist.common.api.model.KindedMime;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filesystem.backend.FileState;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.kind.model.Affordance;
import io.pantheist.handler.kind.model.AffordanceType;
import io.pantheist.handler.kind.model.HookType;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.handler.kind.model.KindHandler;
import io.pantheist.handler.kind.model.KindHook;
import io.pantheist.plugin.interfaces.AlteredSnapshot;

final class FileKindHandlerImpl implements FileKindHandler
{
	private static final String KIND = "kind";
	private static final Logger LOGGER = LogManager.getLogger(FileKindHandlerImpl.class);
	private static final String LOCATION_IN_PROJECT_DIR = "locationInProjectDir";
	private final FilesystemStore filesystem;
	private final UrlTranslation urlTranslation;
	private final ObjectMapper objectMapper;
	private final CommonApiModelFactory commonFactory;

	// State
	private final Map<String, Consumer<AlteredSnapshot>> changeHookPlugins;

	@Inject
	private FileKindHandlerImpl(
			final FilesystemStore filesystem,
			final UrlTranslation urlTranslation,
			final ObjectMapper objectMapper,
			final CommonApiModelFactory commonFactory)
	{
		this.filesystem = checkNotNull(filesystem);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.objectMapper = checkNotNull(objectMapper);
		this.commonFactory = checkNotNull(commonFactory);
		this.changeHookPlugins = new HashMap<>();
	}

	private WriteableAlteredSnapshot snapshot(final Kind kind)
	{
		return AlteredSnapshotImpl.of(
				filesystem.snapshot(),
				snap -> changeHook(kind, snap),
				objectMapper,
				baseDir(kind));
	}

	private WriteableAlteredSnapshot snapshotWithoutHooks()
	{
		return AlteredSnapshotImpl.of(
				filesystem.snapshot(),
				x -> {
				},
				objectMapper,
				null);
	}

	private void changeHook(final Kind kind, final AlteredSnapshot snapshot)
	{
		if (kind.computed() != null && kind.computed().hooks() != null)
		{
			for (final KindHook hook : kind.computed().hooks())
			{
				if (hook.type() == HookType.change)
				{
					if (!changeHookPlugins.containsKey(hook.plugin()))
					{
						// Don't make this fatal, otherwise the user might not be able to undo things easily
						LOGGER.warn("No change hook plugin registered: {} for kind {}", hook.plugin(), kind.kindId());
					}

					changeHookPlugins.get(hook.plugin()).accept(snapshot);
				}
			}
		}
	}

	@Override
	public Possible<String> newInstanceOfKind(final Kind kind)
	{
		final FsPath baseDir = baseDir(kind);

		final WriteableAlteredSnapshot snapshot = snapshot(kind);

		final Optional<String> text = blank(kind);

		if (!text.isPresent())
		{
			return FailureReason.KIND_DOES_NOT_SUPPORT.happened();
		}

		for (int i = 1;; i++)
		{
			final String candidateName = "new" + i;
			final FsPath candidate = baseDir.segment(candidateName);
			if (snapshot.checkFileState(candidate) == FileState.DOES_NOT_EXIST)
			{
				snapshot.writeText(candidate, text.get());
				snapshot.writeOutEverything();
				return View.ok(urlTranslation.entityToUrl(kind.kindId(), candidateName));
			}
		}
	}

	private Optional<String> blank(final Kind kind)
	{
		for (final Affordance affordance : kind.affordances())
		{
			if (affordance.type() == AffordanceType.blank
					&& affordance.name() == null
					&& affordance.prototypeValue() != null)
			{
				try
				{
					return Optional.of(objectMapper.writeValueAsString(affordance.prototypeValue()));
				}
				catch (final JsonProcessingException e)
				{
					throw new FileKindException(e);
				}
			}
		}
		return Optional.empty();
	}

	private Optional<FsPath> optBaseDir(final Kind kind)
	{
		if (kind.computed() == null || kind.computed().handler() != KindHandler.file)
		{
			return Optional.empty();
		}
		if (kind.schema().identification() == null)
		{
			return Optional.empty();
		}
		if (!kind.schema().identification().has(LOCATION_IN_PROJECT_DIR) ||
				!kind.schema().identification().get(LOCATION_IN_PROJECT_DIR).isTextual())
		{
			return Optional.empty();
		}
		final String locationInProjectDir = kind.schema().identification().get(LOCATION_IN_PROJECT_DIR).textValue();

		final FsPath baseDir = filesystem.projectBucket().slashSeparatedSegments(locationInProjectDir);
		return Optional.of(baseDir);
	}

	private FsPath baseDir(final Kind kind)
	{
		return optBaseDir(kind)
				.orElseThrow(() -> new FileKindException("No location specified for items of kind " + kind.kindId()));
	}

	@Override
	public AntiIterator<ObjectNode> discoverFileEntities(final Kind kind)
	{
		final FsPath baseDir = baseDir(kind);

		return consumer -> {
			final WriteableAlteredSnapshot snapshot = snapshot(kind);

			snapshot.listFilesAndDirectories(baseDir)
					.filter(p -> snapshot.safeIsFile(p))
					.map(p -> toJsonNode(snapshot, p, kind))
					.forEach(consumer);

			snapshot.writeOutEverything();
		};
	}

	@Override
	public AntiIterator<String> listAllEntityTexts(final Kind kind)
	{
		final FsPath baseDir = baseDir(kind);

		return consumer -> {
			final WriteableAlteredSnapshot snapshot = snapshot(kind);

			snapshot.listFilesAndDirectories(baseDir)
					.filter(p -> snapshot.safeIsFile(p))
					.map(snapshot::readText)
					.forEach(consumer);

			snapshot.writeOutEverything();
		};
	}

	private ObjectNode toJsonNode(final AlteredSnapshot snapshot, final FsPath p, final Kind kind)
	{
		return objectMapper.getNodeFactory().objectNode()
				.put("kindId", kind.kindId())
				.put("entityId", p.lastSegment());
	}

	@Override
	public Possible<KindedMime> getEntity(final Kind kind, final String entityId)
	{
		final WriteableAlteredSnapshot snapshot = snapshot(kind);

		if (kind.computed().mimeType() == null)
		{
			return FailureReason.KIND_IS_INVALID.happened();
		}

		return getText(snapshot, kind, entityId).map(text -> {
			return commonFactory.kindedMime(
					urlTranslation.kindToUrl(kind.kindId()),
					kind.computed().mimeType(),
					text);
		});
	}

	private FsPath path(final Kind kind, final String entityId)
	{
		return baseDir(kind).segment(entityId);
	}

	private Possible<String> getText(final AlteredSnapshot snapshot, final Kind kind, final String entityId)
	{
		final FsPath path = path(kind, entityId);

		if (snapshot.isFile(path))
		{
			final String text = snapshot.readText(path);
			return View.ok(text);
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	private Optional<Affordance> findAdd(final Kind kind, final String addName)
	{
		checkNotNull(kind);
		OtherPreconditions.checkNotNullOrEmpty(addName);
		for (final Affordance affordance : kind.affordances())
		{
			if (affordance.type() == AffordanceType.add
					&& addName.equals(affordance.name()))
			{
				return Optional.of(affordance);
			}
		}
		return Optional.empty();
	}

	@Override
	public Possible<Void> add(final Kind kind, final String entityId, final String addName)
	{
		final Optional<Affordance> aff = findAdd(kind, addName);

		if (!aff.isPresent())
		{
			return FailureReason.KIND_DOES_NOT_SUPPORT.happened();
		}

		final WriteableAlteredSnapshot snapshot = snapshot(kind);
		final FsPath path = path(kind, entityId);
		return getText(snapshot, kind, entityId).posMap(text -> {
			final JsonNode node;
			try
			{
				node = objectMapper.readValue(text, JsonNode.class);
			}
			catch (final IOException e)
			{
				return FailureReason.OPERATING_ON_INVALID_ENTITY.happened();
			}
			return performAdd(kind, node, aff.get()).map(x -> {
				snapshot.writeJson(path, node);
				snapshot.writeOutEverything();
				return null;
			});
		});
	}

	private Possible<Void> performAdd(final Kind kind, final JsonNode document, final Affordance aff)
	{
		if (aff.location() == null)
		{
			return FailureReason.KIND_IS_INVALID.happened();
		}
		final JsonNode node = document.at(aff.location().pointer());
		if (node.isMissingNode())
		{
			return FailureReason.OPERATING_ON_INVALID_ENTITY.happened();
		}
		if (!node.isArray())
		{
			return FailureReason.OPERATING_ON_INVALID_ENTITY.happened();
		}
		if (aff.prototypeValue() == null)
		{
			return FailureReason.KIND_IS_INVALID.happened();
		}
		((ArrayNode) node).add(aff.prototypeValue());

		return View.noContent();
	}

	@Override
	public Possible<Void> putEntity(final Kind kind, final String entityId, final String text,
			final boolean failIfExists)
	{
		final FsPath path = path(kind, entityId);
		final WriteableAlteredSnapshot snapshot = snapshot(kind);

		if (snapshot.isFile(path) && failIfExists)
		{
			return FailureReason.ALREADY_EXISTS.happened();
		}
		snapshot.writeText(path, text);
		snapshot.writeOutEverything();
		return View.noContent();
	}

	@Override
	public Possible<Void> deleteEntity(final Kind kind, final String entityId)
	{
		final FsPath path = path(kind, entityId);
		final WriteableAlteredSnapshot snapshot = snapshot(kind);
		if (snapshot.isFile(path))
		{
			snapshot.deleteFile(path);
			snapshot.writeOutEverything();
			return View.noContent();
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public void registerChangeHookPlugin(final String pluginId, final Consumer<AlteredSnapshot> pluginHook)
	{
		this.changeHookPlugins.put(pluginId, pluginHook);
	}

	@Override
	public void deregisterAllPlugins()
	{
		this.changeHookPlugins.clear();
	}

	@Override
	public void deregisterPlugin(final String pluginId)
	{
		this.changeHookPlugins.remove(pluginId);
	}

	/**
	 * I don't really know what to do about this one. The path for kinds needs to be
	 * hardcoded here, because otherwise it doesn't know where to look for them.
	 * There's probably a more elegant solution.
	 */
	@Override
	public Optional<Kind> getKind(final String kindId)
	{
		final FsPath path = baseKindDir().segment(kindId);
		final AlteredSnapshot snapshot = snapshotWithoutHooks();
		return getKindFromSnapshot(path, snapshot);
	}

	private FsPath baseKindDir()
	{
		return filesystem.projectBucket().segment(KIND);
	}

	private Optional<Kind> getKindFromSnapshot(final FsPath path, final AlteredSnapshot snapshot)
	{
		final String kindId = path.segmentsRelativeTo(baseKindDir()).failIfMultiple().get();
		if (snapshot.safeIsFile(path))
		{
			final String text = snapshot.readText(path);
			try
			{
				final Kind kind = objectMapper.readValue(text, Kind.class);
				if (!kindId.equals(kind.kindId()))
				{
					// Kinds that specify the wrong kindId will cause problems, so
					// discard them at this stage.
					LOGGER.warn("Wrong kindId for kind {}. It said {}", kindId, kind.kindId());
					return Optional.empty();
				}
				return Optional.of(kind);
			}
			catch (final IOException e)
			{
				LOGGER.warn("Parse problem for kind {}", kindId);
				LOGGER.catching(e);
				return Optional.empty();
			}
		}
		else
		{
			return Optional.empty();
		}
	}

	@Override
	public AntiIterator<Kind> listAllKinds()
	{
		return consumer -> {
			final WriteableAlteredSnapshot snapshot = snapshot(metakind());
			snapshot
					.listFilesAndDirectories(snapshot.baseDir())
					.optMap(path -> getKindFromSnapshot(path, snapshot))
					.forEach(consumer);
			snapshot.writeOutEverything();
		};
	}

	@Override
	public Kind metakind()
	{
		return getKind(KIND).get();
	}

	private void sendChangeSignal(final Kind kind)
	{
		if (optBaseDir(kind).isPresent())
		{
			final WriteableAlteredSnapshot snapshot = snapshot(kind);
			snapshot.forceChangeNotification();
			snapshot.writeOutEverything();
		}
	}

	@Override
	public void sendGlobalChangeSignal()
	{
		final List<Kind> kinds = listAllKinds().toArrayList();

		// Deal with "kind" first, in case anything else relies on it.
		for (int i = 0; i < kinds.size(); i++)
		{
			if (kinds.get(i).kindId().equals(KIND))
			{
				sendChangeSignal(kinds.get(i));
				kinds.remove(i);
				break;
			}
		}

		// Now deal with the rest.
		kinds.forEach(this::sendChangeSignal);
	}

}
