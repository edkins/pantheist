package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import javax.inject.Inject;

import restless.common.util.DummyException;
import restless.handler.filesystem.except.FsConflictException;
import restless.handler.filesystem.except.FsIoException;
import restless.handler.filesystem.except.FsUnexpectedStateException;
import restless.system.config.RestlessConfig;

final class FilesystemSnapshotImpl implements FilesystemSnapshot, FsPathMapping
{
	private final Lock lock;
	private final RestlessConfig config;
	private final long timestamp;

	//State
	private final Map<FsPath, FileState> knownFileStates;
	private boolean written;
	private final List<FsPath> directoriesToCreate;

	@Inject
	private FilesystemSnapshotImpl(@FilesystemLock final Lock lock, final RestlessConfig config)
	{
		this.lock = checkNotNull(lock);
		this.config = checkNotNull(config);
		this.timestamp = System.currentTimeMillis();
		this.knownFileStates = new HashMap<>();
		this.written = false;
		this.directoriesToCreate = new ArrayList<>();
	}

	@Override
	public <T> T read(final FsPath path, final InputSteamProcessor<T> fn)
	{
		lock.lock();
		try
		{
			if (!checkFileState(path).equals(FileState.REGULAR_FILE))
			{
				throw new FsIoException("Not a regular file: " + path);
			}
			try (InputStream inputStream = new FileInputStream(find(path)))
			{
				return fn.process(inputStream);
			}
			catch (final IOException e)
			{
				throw new FsIoException(e);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public FileState checkFileState(final FsPath path)
	{
		if (written)
		{
			throw new IllegalStateException("Already written to this snapshot");
		}
		return internalCheckFileState(path);
	}

	private FileState internalCheckFileState(final FsPath path)
	{
		final File file = find(path);

		final long lastModified = file.lastModified();
		if (lastModified >= timestamp)
		{
			throw conflict(path, lastModified);
		}

		final FileState result = obtainFileState(file);
		putFileState(path, result);
		return result;
	}

	private FileState obtainFileState(final File file)
	{
		if (file.isFile())
		{
			return FileState.REGULAR_FILE;
		}
		else if (file.isDirectory())
		{
			return FileState.DIRECTORY;
		}
		else if (!file.exists())
		{
			return FileState.DOES_NOT_EXIST;
		}
		else
		{
			return FileState.UNKNOWN;
		}
	}

	private void putFileState(final FsPath path, final FileState state)
	{
		final FileState oldState = knownFileStates.get(path);
		if (oldState != null && !oldState.equals(state))
		{
			throw new FsConflictException(
					String.format("Snapshot %s, old state %s, new state %s, path %s", new Date(timestamp), oldState,
							state, path));
		}
		knownFileStates.put(path, state);
	}

	private DummyException conflict(final FsPath path, final long lastModified)
	{
		throw new FsConflictException(
				String.format("Snapshot %s, modified %s, path %s", new Date(timestamp), new Date(lastModified), path));
	}

	private File find(final FsPath path)
	{
		return path.in(config.dataDir());
	}

	@Override
	public void write(final FileProcessor fn)
	{
		if (written)
		{
			throw new IllegalStateException("Already written to this snapshot");
		}
		written = true;
		lock.lock();
		try
		{
			for (final FsPath path : knownFileStates.keySet())
			{
				// This will check each file is in the same state that it was (if known), and that it wasn't modified
				// since the timestamp.
				internalCheckFileState(path);
			}

			final FsPathMapping map = this;
			createMissingDirectories(directoriesToCreate, map);
			fn.process(map);
		}
		catch (final IOException ex)
		{
			throw new FsIoException(ex);
		}
		finally
		{
			lock.unlock();
		}
	}

	// This is really just a convenience function, so I've made it static to avoid exposing
	// internals of FilesystemSnapshotImpl. It has access to the same FsPathMapping that the
	// user-supplied function would have.
	private static void createMissingDirectories(final List<FsPath> directoriesToCreate,
			final FsPathMapping map)
	{
		for (final FsPath path : directoriesToCreate)
		{
			map.get(path).mkdir();
		}
	}

	@Override
	public boolean isDir(final FsPath path)
	{
		final FileState state = checkFileState(path);
		switch (state) {
		case DIRECTORY:
			return true;
		case DOES_NOT_EXIST:
			return false;
		default:
			throw new FsUnexpectedStateException("Expected directory, got " + state);
		}
	}

	@Override
	public boolean isFile(final FsPath path)
	{
		final FileState state = checkFileState(path);
		switch (state) {
		case REGULAR_FILE:
			return true;
		case DOES_NOT_EXIST:
			return false;
		default:
			throw new FsUnexpectedStateException("Expected file, got " + state);
		}
	}

	@Override
	public boolean parentDirectoryExists(final FsPath path)
	{
		if (path.isEmpty())
		{
			throw new IllegalArgumentException("parentDirectoryExists: empty path");
		}
		return isDir(path.parent());
	}

	@Override
	public void writeSingle(final FsPath path, final SingleFileProcessor fn)
	{
		write(map -> fn.process(map.get(path)));
	}

	@Override
	public File get(final FsPath path)
	{
		if (knownFileStates.containsKey(path))
		{
			return find(path);
		}
		else
		{
			throw new IllegalStateException("Did not previously check state of " + path);
		}
	}

	@Override
	public void willNeedDirectory(final FsPath path)
	{
		path.leadingPortions().forEach(dir -> {
			if (!directoriesToCreate.contains(dir) && !isDir(dir))
			{
				directoriesToCreate.add(dir);
			}
		});
	}

}
