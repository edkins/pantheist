package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import restless.common.util.OtherPreconditions;
import restless.handler.binding.backend.ManagementFunctions;

final class FilesystemStoreImpl implements FilesystemStore
{
	final ObjectMapper objectMapper;

	// State
	private final FilesystemFactory factory;

	@Inject
	FilesystemStoreImpl(final ObjectMapper objectMapper, final FilesystemFactory factory)
	{
		this.objectMapper = checkNotNull(objectMapper);
		this.factory = checkNotNull(factory);
	}

	@Override
	public void initialize()
	{
		final FilesystemSnapshot snapshot = snapshot();
		final FsPath path = systemBucket();
		if (!snapshot.isDir(path))
		{
			snapshot.write(ImmutableMap.of(path, File::mkdir));
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
	public FsPath newBucket(final String nameHint)
	{
		OtherPreconditions.checkNotNullOrEmpty(nameHint);
		final FilesystemSnapshot snapshot = snapshot();
		for (int i = 0;; i++)
		{
			final FsPath candidate = fromBucketName(nameHint + i);
			if (!snapshot.isDir(candidate))
			{
				snapshot.write(ImmutableMap.of(candidate, File::mkdir));
				return candidate;
			}
		}
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
