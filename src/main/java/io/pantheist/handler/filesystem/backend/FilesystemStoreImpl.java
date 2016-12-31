package io.pantheist.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.system.config.PantheistConfig;

final class FilesystemStoreImpl implements FilesystemStore
{
	final ObjectMapper objectMapper;
	private final FilesystemFactory factory;
	private final PantheistConfig config;

	@Inject
	FilesystemStoreImpl(
			final ObjectMapper objectMapper,
			final FilesystemFactory factory,
			final PantheistConfig config)
	{
		this.objectMapper = checkNotNull(objectMapper);
		this.factory = checkNotNull(factory);
		this.config = checkNotNull(config);
	}

	@Override
	public void initialize()
	{
		final FilesystemSnapshot snapshot = snapshot();
		final FsPath path = systemBucket();
		snapshot.willNeedDirectory(path);

		// Dummy write to make sure the willNeedDirectory took effect.
		snapshot.write(x -> {
		});
	}

	@Override
	public FsPath systemBucket()
	{
		return rootPath().slashSeparatedSegments(config.relativeSystemPath());
	}

	@Override
	public FsPath srvBucket()
	{
		return rootPath().slashSeparatedSegments(config.relativeSrvPath());
	}

	@Override
	public FsPath rootPath()
	{
		return FsPathImpl.empty();
	}

	@Override
	public FilesystemSnapshot snapshot()
	{
		return factory.snapshot();
	}

	@Override
	public <T> JsonSnapshot<T> jsonSnapshot(final FsPath path, final Class<T> clazz)
	{
		return new JsonSnapshotImpl<>(snapshot(), objectMapper, path, clazz);
	}

	@Override
	public Possible<String> getSrvData(final String relativePath)
	{
		final FilesystemSnapshot snapshot = snapshot();
		final FsPath path = srvBucket().slashSeparatedSegments(relativePath);
		if (snapshot.isFile(path))
		{
			final String data = snapshot.readText(path);
			return View.ok(data);
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<Void> putSrvData(final String relativePath, final String data)
	{
		final FilesystemSnapshot snapshot = snapshot();
		final FsPath path = srvBucket().slashSeparatedSegments(relativePath);
		snapshot.willNeedDirectory(path.parent());
		snapshot.isFile(path); // throws an exception if it exists but is not a regular file
		snapshot.writeSingleText(path, data);
		return View.noContent();
	}
}
