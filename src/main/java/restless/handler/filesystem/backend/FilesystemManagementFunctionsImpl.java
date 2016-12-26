package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.inject.assistedinject.Assisted;

import restless.handler.binding.backend.ManagementFunctions;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.backend.PossibleEmpty;

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
		final FilesystemSnapshot snapshot = store.snapshot();
		if (snapshot.isFile(path))
		{
			final String data = snapshot.read(path, in -> IOUtils.toString(in, StandardCharsets.UTF_8));
			return PossibleData.of(data);
		}
		else
		{
			return PossibleData.doesNotExist();
		}
	}

	@Override
	public PossibleEmpty putString(final String data)
	{
		final FilesystemSnapshot snapshot = store.snapshot();
		snapshot.willNeedDirectory(path.parent());
		snapshot.isFile(path); // throws an exception if it exists but is not a regular file
		snapshot.writeSingle(path, file -> FileUtils.writeStringToFile(file, data, StandardCharsets.UTF_8));
		return PossibleEmpty.ok();
	}

}
