package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.google.inject.assistedinject.Assisted;

import restless.handler.binding.backend.ManagementFunctions;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.backend.PossibleEmpty;
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
	public PossibleData getString()
	{
		try (LockedFile f = store.lock(path))
		{
			if (f.fileExists())
			{
				final String data = IOUtils.toString(f.inputStream(), StandardCharsets.UTF_8);
				return PossibleData.of(data);
			}
			else
			{
				return PossibleData.doesNotExist();
			}
		}
		catch (final IOException e)
		{
			throw new FsIoException(e);
		}
	}

	@Override
	public PossibleEmpty putString(final String data)
	{
		try (LockedFile f = store.lock(path))
		{
			IOUtils.write(data, f.outputStream(), StandardCharsets.UTF_8);
			return PossibleEmpty.ok();
		}
		catch (final IOException e)
		{
			throw new FsIoException(e);
		}
	}

}
