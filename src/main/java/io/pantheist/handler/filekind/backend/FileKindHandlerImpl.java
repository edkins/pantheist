package io.pantheist.handler.filekind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.common.api.model.CommonApiModelFactory;
import io.pantheist.common.api.model.KindedMime;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.FilterableObjectStream;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filesystem.backend.FileState;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.kind.model.Affordance;
import io.pantheist.handler.kind.model.AffordanceType;
import io.pantheist.handler.kind.model.Kind;

final class FileKindHandlerImpl implements FileKindHandler
{
	private static final String LOCATION_IN_PROJECT_DIR = "locationInProjectDir";
	private final FilesystemStore filesystem;
	private final UrlTranslation urlTranslation;
	private final ObjectMapper objectMapper;
	private final CommonApiModelFactory commonFactory;

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
	}

	@Override
	public Possible<String> newInstanceOfKind(final Kind kind)
	{
		final FsPath baseDir = baseDir(kind);

		final FilesystemSnapshot snapshot = filesystem.snapshot();

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
				snapshot.writeSingleText(candidate, text.get());
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

	private FsPath baseDir(final Kind kind)
	{
		if (kind.schema().identification() == null)
		{
			throw new FileKindException("No location specified for items of kind " + kind.kindId());
		}
		if (!kind.schema().identification().has(LOCATION_IN_PROJECT_DIR) ||
				!kind.schema().identification().get(LOCATION_IN_PROJECT_DIR).isTextual())
		{
			throw new FileKindException("No location specified for items of kind " + kind.kindId());
		}
		final String locationInProjectDir = kind.schema().identification().get(LOCATION_IN_PROJECT_DIR).textValue();

		final FsPath baseDir = filesystem.projectBucket().slashSeparatedSegments(locationInProjectDir);
		return baseDir;
	}

	@Override
	public FilterableObjectStream discoverFileEntities(final Kind kind)
	{
		final FsPath baseDir = baseDir(kind);
		final FilesystemSnapshot snapshot = filesystem.snapshot();

		return snapshot.listFilesAndDirectories(baseDir)
				.filter(p -> snapshot.safeIsFile(p))
				.map(p -> toJsonNode(snapshot, p, kind))
				.toDumbFilterableStream();
	}

	private ObjectNode toJsonNode(final FilesystemSnapshot snapshot, final FsPath p, final Kind kind)
	{
		return objectMapper.getNodeFactory().objectNode()
				.put("kindId", kind.kindId())
				.put("entityId", p.lastSegment());
	}

	@Override
	public Possible<KindedMime> getEntity(final Kind kind, final String entityId)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();

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

	private Possible<String> getText(final FilesystemSnapshot snapshot, final Kind kind, final String entityId)
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

		final FilesystemSnapshot snapshot = filesystem.snapshot();
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
				snapshot.writeSingle(path, snapshot.jsonWriter(node));
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
	public Possible<Void> putEntity(final Kind kind, final String entityId, final String text)
	{
		final FsPath path = path(kind, entityId);
		final FilesystemSnapshot snapshot = filesystem.snapshot();

		snapshot.isFile(path);
		snapshot.writeSingleText(path, text);
		return View.noContent();
	}

	@Override
	public Possible<Void> deleteEntity(final Kind kind, final String entityId)
	{
		final FsPath path = path(kind, entityId);
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		if (snapshot.isFile(path))
		{
			final AtomicBoolean success = new AtomicBoolean(false);
			snapshot.writeSingle(path, file -> success.set(file.delete()));
			if (!success.get())
			{
				return FailureReason.IO_PROBLEM.happened();
			}
			return View.noContent();
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

}
