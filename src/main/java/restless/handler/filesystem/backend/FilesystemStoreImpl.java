package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import restless.common.util.OtherPreconditions;
import restless.handler.binding.backend.ManagementFunctions;
import restless.handler.filesystem.except.FsIoException;
import restless.system.config.RestlessConfig;

final class FilesystemStoreImpl implements FilesystemStore
{
	final ObjectMapper objectMapper;
	private final RestlessConfig config;
	private final FilesystemFactory factory;

	@Inject
	FilesystemStoreImpl(final ObjectMapper objectMapper,
			final FilesystemFactory factory,
			final RestlessConfig config)
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
		if (!snapshot.isDir(path))
		{
			snapshot.writeSingle(path, File::mkdir);
		}
		writeResourceFiles();
	}

	private void writeResourceFiles()
	{
		final Map<String, FsPath> stuff = new HashMap<>();

		final FilesystemSnapshot snapshot = snapshot();
		final FsPath resourceFileDir = systemBucket().segment("resource-files");
		snapshot.isDir(resourceFileDir);
		for (final String filename : config.resourceFiles())
		{
			OtherPreconditions.checkNotNullOrEmpty(filename);
			final String resourcePath = "/resource-files/" + filename;
			final FsPath path = resourceFileDir.segment(filename);

			snapshot.isFile(path);
			stuff.put(resourcePath, path);
		}
		snapshot.write(map -> {
			map.get(resourceFileDir).mkdir();
			for (final Entry<String, FsPath> entry : stuff.entrySet())
			{
				copyResourceFileTo(entry.getKey(), map.get(entry.getValue()));
			}
		});
	}

	private void copyResourceFileTo(final String resourcePath, final File file) throws IOException
	{
		try (InputStream input = FilesystemStoreImpl.class.getResourceAsStream(resourcePath);
				OutputStream output = new FileOutputStream(file))
		{
			if (input == null)
			{
				throw new FsIoException("Missing resource: " + resourcePath);
			}
			IOUtils.copyLarge(input, output);
		}
	}

	private FsPath fromBucketName(final String bucketName)
	{
		return FsPathImpl.empty().segment(bucketName);
	}

	@Override
	public ManagementFunctions manage(final FsPath path)
	{
		return factory.managementFunctions(path);
	}

	@Override
	public FsPath systemBucket()
	{
		return fromBucketName("system");
	}

	@Override
	public FsPath srvBucket()
	{
		return fromBucketName("srv");
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
}
