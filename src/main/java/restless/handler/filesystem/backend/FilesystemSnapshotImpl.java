package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import restless.common.util.AntiIt;
import restless.common.util.AntiIterator;
import restless.common.util.DummyException;
import restless.handler.filesystem.except.FsConflictException;
import restless.handler.filesystem.except.FsIoException;
import restless.handler.filesystem.except.FsUnexpectedStateException;
import restless.system.config.RestlessConfig;

final class FilesystemSnapshotImpl implements FilesystemSnapshot, FsPathMapping
{
	private final Lock lock;
	private final RestlessConfig config;
	private final ObjectMapper objectMapper;
	private final long timestamp;

	//State
	private final Map<FsPath, FileState> knownFileStates;
	private boolean written;
	private final List<IncidentalWriteTask> incidentalWriteTasks;

	@Inject
	private FilesystemSnapshotImpl(@FilesystemLock final Lock lock,
			final RestlessConfig config,
			final ObjectMapper objectMapper)
	{
		this.lock = checkNotNull(lock);
		this.config = checkNotNull(config);
		this.timestamp = System.currentTimeMillis();
		this.knownFileStates = new HashMap<>();
		this.written = false;
		this.incidentalWriteTasks = new ArrayList<>();
		this.objectMapper = checkNotNull(objectMapper);
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
			performIncidentalWriteTasks(incidentalWriteTasks, map);
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
	private static void performIncidentalWriteTasks(final List<IncidentalWriteTask> incidentalWriteTasks,
			final FsPathMapping map) throws IOException
	{
		for (final IncidentalWriteTask task : incidentalWriteTasks)
		{
			task.processor().process(map.get(task.path()));
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
	public boolean haveIncidentalWriteTask(final FsPath path)
	{
		return incidentalWriteTasks.stream().anyMatch(t -> t.path().equals(path));
	}

	@Override
	public void willNeedDirectory(final FsPath path)
	{
		path.leadingPortions().forEach(dir -> {
			if (!haveIncidentalWriteTask(dir) && !isDir(dir))
			{
				incidentalWriteTasks.add(new IncidentalWriteTask(path, file -> file.mkdir()));
			}
		});
	}

	@Override
	public String readText(final FsPath file)
	{
		return read(file, in -> IOUtils.toString(in, StandardCharsets.UTF_8));
	}

	@Override
	public void writeSingleText(final FsPath path, final String text)
	{
		writeSingle(path, file -> FileUtils.writeStringToFile(file, text, StandardCharsets.UTF_8));
	}

	@Override
	public AntiIterator<FsPath> listFilesAndDirectories(final FsPath dir)
	{
		if (isDir(dir))
		{
			return consumer -> {
				for (final String segment : find(dir).list())
				{
					final FsPath childPath = dir.segment(segment);
					checkFileState(childPath);
					consumer.accept(childPath);
				}
			};
		}
		else
		{
			return AntiIt.empty();
		}
	}

	@Override
	public <T> T readJson(final FsPath path, final Class<T> clazz)
	{
		return read(path, input -> objectMapper.readValue(input, clazz));
	}

	private void recurseFilesAndDirectories(final FsPath path, final Consumer<FsPath> consumer)
	{
		switch (checkFileState(path)) {
		case REGULAR_FILE:
			consumer.accept(path);
			break;
		case DIRECTORY:
			consumer.accept(path);
			for (final String segment : find(path).list())
			{
				final FsPath childPath = path.segment(segment);
				recurseFilesAndDirectories(childPath, consumer);
			}
			break;
		case DOES_NOT_EXIST:
		default:
			// don't supply path if it doesn't exist is or an unknown type of filesystem object
			break;
		}
	}

	@Override
	public AntiIterator<FsPath> recurse(final FsPath path)
	{
		return consumer -> recurseFilesAndDirectories(path, consumer);
	}

	@Override
	public boolean safeIsFile(final FsPath path)
	{
		return checkFileState(path) == FileState.REGULAR_FILE;
	}

	@Override
	public boolean safeIsDir(final FsPath path)
	{
		return checkFileState(path) == FileState.DIRECTORY;
	}

	@Override
	public void incidentalWriteTask(final FsPath path, final SingleFileProcessor task)
	{
		checkNotNull(path);
		checkNotNull(task);
		if (haveIncidentalWriteTask(path))
		{
			throw new IllegalStateException("Already have incidental write task for path " + path);
		}
		incidentalWriteTasks.add(new IncidentalWriteTask(path, task));
	}

	@Override
	public <T> SingleFileProcessor jsonWriter(final T obj)
	{
		return file -> objectMapper.writeValue(file, obj);
	}

}
