package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.inject.Inject;

import restless.common.util.Cleanup;
import restless.common.util.DummyException;
import restless.handler.filesystem.except.FsConflictException;
import restless.handler.filesystem.except.FsIoException;
import restless.handler.filesystem.except.FsUnexpectedStateException;
import restless.system.config.RestlessConfig;

final class FilesystemSnapshotImpl implements FilesystemSnapshot
{
	private final Lock lock;
	private final RestlessConfig config;
	private final long timestamp;

	//State
	private final Map<FsPath, FileState> knownFileStates;
	private boolean written;

	@Inject
	private FilesystemSnapshotImpl(@FilesystemLock final Lock lock, final RestlessConfig config)
	{
		this.lock = checkNotNull(lock);
		this.config = checkNotNull(config);
		this.timestamp = System.currentTimeMillis();
		this.knownFileStates = new HashMap<>();
		this.written = false;
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
	public void write(final Map<FsPath, FileProcessor> fns)
	{
		if (written)
		{
			throw new IllegalStateException("Already written to this snapshot");
		}
		written = true;
		lock.lock();
		try
		{
			final Set<FsPath> interestingFiles = new HashSet<>();
			interestingFiles.addAll(knownFileStates.keySet());
			interestingFiles.addAll(fns.keySet());
			for (final FsPath path : interestingFiles)
			{
				// This will check each file is in the same state that it was (if known), and that it wasn't modified
				// since the timestamp.
				checkFileState(path);
			}

			Cleanup.run(
					fns.entrySet()
							.stream()
							.map(this::task)
							.collect(Collectors.toList()));
		}
		finally
		{
			lock.unlock();
		}
	}

	private Runnable task(final Entry<FsPath, FileProcessor> entry)
	{
		return () -> {
			try
			{
				entry.getValue().process(find(entry.getKey()));
			}
			catch (final IOException e)
			{
				throw new FsIoException(e);
			}
		};
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

}
