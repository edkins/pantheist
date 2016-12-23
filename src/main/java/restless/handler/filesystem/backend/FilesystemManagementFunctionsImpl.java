package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.google.inject.assistedinject.Assisted;

import restless.handler.binding.backend.ManagementFunctions;
import restless.handler.filesystem.except.FsIoException;

final class FilesystemManagementFunctionsImpl implements ManagementFunctions
{
	private final FilesystemStore store;
	private final FsPath path;

	@Inject
	private FilesystemManagementFunctionsImpl(
			final FilesystemStore store,
			@Assisted final FsPath path)
	{
		this.store = checkNotNull(store);
		this.path = checkNotNull(path);
	}

	@Override
	public String getString()
	{
		try (LockedFile f = store.lock(path))
		{
			return IOUtils.toString(f.inputStream(), StandardCharsets.UTF_8);
		}
		catch (final IOException e)
		{
			throw new FsIoException(e);
		}
	}

	@Override
	public void putString(final String data)
	{
		try (LockedFile f = store.lock(path))
		{
			IOUtils.write(data, f.outputStream(), StandardCharsets.UTF_8);
		}
		catch (final IOException e)
		{
			throw new FsIoException(e);
		}
	}

}
