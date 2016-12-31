package restless.api.flatdir.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import restless.api.flatdir.model.ApiFlatDirModelFactory;
import restless.api.flatdir.model.ListFileItem;
import restless.api.flatdir.model.ListFileResponse;
import restless.api.flatdir.model.ListFlatDirItem;
import restless.api.flatdir.model.ListFlatDirResponse;
import restless.common.api.model.CommonApiModelFactory;
import restless.common.api.model.ListClassifierResponse;
import restless.common.api.url.UrlTranslation;
import restless.common.util.FailureReason;
import restless.common.util.OtherPreconditions;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.filesystem.backend.FilesystemSnapshot;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;

final class FlatDirBackendImpl implements FlatDirBackend
{
	private final FilesystemStore filesystem;
	private final ApiFlatDirModelFactory modelFactory;
	private final UrlTranslation urlTranslation;
	private final CommonApiModelFactory commonFactory;

	@Inject
	private FlatDirBackendImpl(
			final FilesystemStore filesystem,
			final ApiFlatDirModelFactory modelFactory,
			final UrlTranslation urlTranslation,
			final CommonApiModelFactory commonFactory)
	{
		this.filesystem = checkNotNull(filesystem);
		this.modelFactory = checkNotNull(modelFactory);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.commonFactory = checkNotNull(commonFactory);
	}

	private ListFileItem toListFileItem(final FsPath fsPath)
	{
		final String dir = fsPathToDir(fsPath.parent());
		final String file = fsPath.lastSegment();
		return modelFactory.listFileItem(urlTranslation.flatDirFileToUrl(dir, file), file);
	}

	private String fsPathToDir(final FsPath fsPath)
	{
		if (fsPath.isEmpty())
		{
			return "/";
		}
		else
		{
			return fsPath.toString();
		}
	}

	private FsPath dirToFsPath(final String dir)
	{
		OtherPreconditions.checkNotNullOrEmpty(dir);
		if (dir.equals("/"))
		{
			// Special representation for root
			// It's only the FlatDirResource which wants this represented as a slash,
			// in most other places internally we would represent it as the empty string.
			return filesystem.rootPath();
		}
		else
		{
			return filesystem.rootPath().slashSeparatedSegments(dir);
		}
	}

	@Override
	public Possible<ListFileResponse> listFiles(final String dir)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		final FsPath path = dirToFsPath(dir);
		if (snapshot.safeIsDir(path))
		{
			return View.ok(snapshot.listFilesAndDirectories(path)
					.filter(snapshot::safeIsFile)
					.map(this::toListFileItem)
					.wrap(modelFactory::listFileResponse));

		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<ListClassifierResponse> listFlatDirClassifiers(final String dir)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		final FsPath path = dirToFsPath(dir);
		if (snapshot.safeIsDir(path))
		{
			return View.ok(
					commonFactory.listClassifierResponse(
							urlTranslation.listFlatDirClassifiers(dir)));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	private ListFlatDirItem toListFlatDirItem(final FsPath fsPath)
	{
		final String dir = fsPathToDir(fsPath);
		return modelFactory.listFlatDirItem(urlTranslation.flatDirToUrl(dir), dir);
	}

	@Override
	public ListFlatDirResponse listFlatDirs()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		return snapshot.recurse(filesystem.rootPath())
				.filter(snapshot::safeIsDir)
				.map(this::toListFlatDirItem)
				.wrap(modelFactory::listFlatDirResponse);
	}

}
