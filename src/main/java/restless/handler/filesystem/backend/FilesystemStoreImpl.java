package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Semaphore;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import restless.common.util.OtherPreconditions;
import restless.handler.binding.backend.ManagementFunctions;
import restless.handler.filesystem.except.FsInterruptedException;

final class FilesystemStoreImpl implements FilesystemStoreInterfaces
{
	final ObjectMapper objectMapper;

	// State
	private final Semaphore semaphore;
	private final FilesystemFactory factory;

	@Inject
	FilesystemStoreImpl(final ObjectMapper objectMapper, final FilesystemFactory factory)
	{
		this.objectMapper = checkNotNull(objectMapper);
		this.semaphore = new Semaphore(1);
		this.factory = checkNotNull(factory);
	}

	@Override
	public void initialize()
	{
		createDir(systemBucket());
	}

	private void createDir(final FsPath path)
	{
		try (final LockedFile f = lockRoot())
		{
			for (final FsPathSegment segment : path.segments())
			{
				f.enter(segment);
				f.createDirectoryIfNotPresent();
			}
		}
	}

	private LockedFile lockRoot()
	{
		return lock(FsPathImpl.empty());
	}

	@Override
	public LockedFile lock(final FsPath path)
	{
		try
		{
			semaphore.acquire();
			return factory.lockedFile(path);
		}
		catch (final InterruptedException e)
		{
			throw new FsInterruptedException(e);
		}
	}

	@Override
	public void unlock(final FsPath path)
	{
		semaphore.release();
	}

	@Override
	public <T> LockedTypedFile<T> lockJson(final FsPath path, final Class<T> clazz)
	{
		return LockedJsonFileImpl.from(objectMapper, lock(path), clazz);
	}

	@Override
	public FsPath fromBucketName(final String bucketName)
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
		for (int i = 0;; i++)
		{
			final FsPath candidate = fromBucketName(nameHint + i);
			try (LockedFile f = lock(candidate))
			{
				if (f.attemptNewDirectory())
				{
					return candidate;
				}
			}
		}
	}

	@Override
	public FsPath rootPath()
	{
		return FsPathImpl.empty();
	}
}
