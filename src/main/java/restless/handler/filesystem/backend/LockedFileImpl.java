package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import restless.handler.filesystem.except.FsIoException;
import restless.system.config.RestlessConfig;

final class LockedFileImpl implements LockedFile
{
	private final FilesystemUnlock unlock;
	private final RestlessConfig config;
	// State
	private FsPath path;

	@Inject
	private LockedFileImpl(final FilesystemUnlock unlock,
			final RestlessConfig config,
			@Assisted final FsPath path)
	{
		this.unlock = checkNotNull(unlock);
		this.config = checkNotNull(config);
		this.path = checkNotNull(path);
	}

	@Override
	public void close()
	{
		unlock.unlock(path);
	}

	@Override
	public OutputStream outputStream()
	{
		try
		{
			return new FileOutputStream(file());
		}
		catch (final FileNotFoundException e)
		{
			// This one shouldn't happen though
			throw new FsIoException(e);
		}
	}

	@Override
	public boolean fileExits()
	{
		final File file = file();
		if (file.isFile())
		{
			return true;
		}
		else if (!file.exists())
		{
			return false;
		}
		else
		{
			throw new FsIoException("Exists but is not a regular file: " + path);
		}
	}

	@Override
	public InputStream inputStream()
	{
		try
		{
			return new FileInputStream(file());
		}
		catch (final FileNotFoundException e)
		{
			throw new FsIoException(e);
		}
	}

	private File file()
	{
		return path.in(config.dataDir());
	}

	@Override
	public void enter(final FsPathSegment segment)
	{
		path = path.segment(segment);
	}

	@Override
	public void createDirectoryIfNotPresent()
	{
		final File file = file();
		if (!file.exists())
		{
			if (!file.mkdir())
			{
				throw new FsIoException("Directory was not created at " + path);
			}
		}
	}

}
