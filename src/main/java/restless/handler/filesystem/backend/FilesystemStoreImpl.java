package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import restless.handler.binding.backend.ManagementFunctions;
import restless.handler.filesystem.except.FsInterruptedException;
import restless.handler.filesystem.except.FsIoException;
import restless.system.config.RestlessConfig;

final class FilesystemStoreImpl implements FilesystemStoreInterfaces
{
	private static final Logger LOGGER = LogManager.getLogger(FilesystemStoreImpl.class);
	private static final FsPathSegment BOUND = FsPathSegmentImpl.fromString("bound");
	private final RestlessConfig config;
	private final ObjectMapper objectMapper;

	// State
	private final Semaphore semaphore;
	private final FilesystemFactory factory;

	@Inject
	FilesystemStoreImpl(final RestlessConfig config,
			final ObjectMapper objectMapper,
			final FilesystemFactory factory)
	{
		this.config = checkNotNull(config);
		this.objectMapper = checkNotNull(objectMapper);
		this.semaphore = new Semaphore(1);
		this.factory = checkNotNull(factory);
	}

	@Override
	public void initialize()
	{
		createPathTo(fsFilesystemPath().segment(BOUND));
		try (LockedTypedFile<FsBoundPaths> f = lockBound())
		{
			if (!f.fileExits())
			{
				f.write(initiallyBoundPaths());
			}
		}
	}

	private LockedTypedFile<FsBoundPaths> lockBound()
	{
		return lockJson(fsFilesystemPath().segment(BOUND), FsBoundPaths.class);
	}

	private FsBoundPaths initiallyBoundPaths()
	{
		return FsBoundPathsImpl.empty().withBoundPath(fsFilesystemPath());
	}

	private FsPath fsFilesystemPath()
	{
		return FsPathImpl.nonempty(config.fsFilesystemPath());
	}

	@Override
	public void bindPath(final FsPath path)
	{
		LOGGER.info("Binding {}", path);
		try (final LockedTypedFile<FsBoundPaths> f = lockBound())
		{
			final FsBoundPaths oldBoundPaths = f.read();
			final FsBoundPaths newBoundPaths = oldBoundPaths.withBoundPath(path);
			f.write(newBoundPaths);
		}

		createPathTo(path);
	}

	private void createPathTo(final FsPath path)
	{
		try (final LockedFile f = lockRoot())
		{
			// Note we create the parent directory but not the binding point itself.
			for (final FsPathSegment segment : path.parent().segments())
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
		return new LockedJsonFileImpl<>(lock(path), clazz);
	}

	private class LockedJsonFileImpl<T> implements LockedTypedFile<T>
	{
		private final LockedFile file;
		private final Class<T> clazz;

		LockedJsonFileImpl(final LockedFile file, final Class<T> clazz)
		{
			this.file = checkNotNull(file);
			this.clazz = checkNotNull(clazz);
		}

		@Override
		public boolean fileExits()
		{
			return file.fileExits();
		}

		@Override
		public void close()
		{
			file.close();
		}

		@Override
		public T read()
		{
			try (InputStream in = file.inputStream())
			{
				return objectMapper.readValue(in, clazz);
			}
			catch (final IOException e)
			{
				throw new FsIoException(e);
			}
		}

		@Override
		public void write(final T value)
		{
			try (OutputStream out = file.outputStream())
			{
				objectMapper.writeValue(out, value);
			}
			catch (final IOException e)
			{
				throw new FsIoException(e);
			}
		}
	}

	@Override
	public FsPath nonemptyPath(final String path)
	{
		return FsPathImpl.nonempty(path);
	}

	@Override
	public ManagementFunctions manage(final FsPath path)
	{
		return factory.managementFunctions(path);
	}
}
