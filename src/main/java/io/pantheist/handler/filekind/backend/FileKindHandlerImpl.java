package io.pantheist.handler.filekind.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.common.api.model.CommonApiModelFactory;
import io.pantheist.common.api.model.KindedMime;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.FilterableObjectStream;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filesystem.backend.FileState;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
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
	public String newInstanceOfKind(final Kind kind)
	{
		final FsPath baseDir = baseDir(kind);

		final FilesystemSnapshot snapshot = filesystem.snapshot();

		final String text = "{}";

		for (int i = 1;; i++)
		{
			final String candidateName = "new" + i;
			final FsPath candidate = baseDir.segment(candidateName);
			if (snapshot.checkFileState(candidate) == FileState.DOES_NOT_EXIST)
			{
				snapshot.writeSingleText(candidate, text);
				return urlTranslation.entityToUrl(kind.kindId(), candidateName);
			}
		}
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
		final FsPath baseDir = baseDir(kind);
		final FilesystemSnapshot snapshot = filesystem.snapshot();

		final FsPath path = baseDir.segment(entityId);

		if (snapshot.isFile(path))
		{
			final String text = snapshot.readText(path);
			return View.ok(commonFactory.kindedMime(
					urlTranslation.kindToUrl(kind.kindId()),
					"application/json",
					text));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

}
