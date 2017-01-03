package io.pantheist.api.flatdir.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import io.pantheist.api.flatdir.model.ApiFlatDirFile;
import io.pantheist.api.flatdir.model.ApiFlatDirModelFactory;
import io.pantheist.api.flatdir.model.ListFileItem;
import io.pantheist.api.flatdir.model.ListFileResponse;
import io.pantheist.api.flatdir.model.ListFlatDirItem;
import io.pantheist.api.flatdir.model.ListFlatDirResponse;
import io.pantheist.common.api.model.CommonApiModelFactory;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;

final class FlatDirBackendImpl implements FlatDirBackend
{
	private static final String SLASH = "/";
	private static final String FILE = "file";
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
		return modelFactory.listFileItem(
				urlTranslation.flatDirFileToUrl(dir, file),
				file,
				urlTranslation.kindToUrl(FILE));
	}

	private String fsPathToDir(final FsPath fsPath)
	{
		if (fsPath.isEmpty())
		{
			return SLASH;
		}
		else
		{
			return fsPath.toString();
		}
	}

	private FsPath dirToFsPath(final String dir)
	{
		OtherPreconditions.checkNotNullOrEmpty(dir);
		if (dir.equals(SLASH))
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
		return modelFactory.listFlatDirItem(
				urlTranslation.flatDirToUrl(dir),
				dir,
				urlTranslation.kindToUrl("directory"));
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

	private FsPath fileToFsPath(final String dir, final String file)
	{
		return dirToFsPath(dir).segment(file);
	}

	@Override
	public Possible<ApiFlatDirFile> getFileInfo(final String dir, final String file)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		final FsPath path = fileToFsPath(dir, file);
		if (snapshot.safeIsFile(path))
		{
			return View.ok(
					modelFactory.file(urlTranslation.flatDirFileDataAction(dir, file), urlTranslation.kindToUrl(FILE)));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<String> getFileData(final String dir, final String file)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		final FsPath path = fileToFsPath(dir, file);
		if (snapshot.safeIsFile(path))
		{
			return View.ok(snapshot.readText(path));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<Void> putFileDataString(final String dir, final String file, final String data,
			final boolean failIfExists)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		final FsPath path = fileToFsPath(dir, file);
		if (snapshot.safeIsFile(path) && failIfExists)
		{
			return FailureReason.ALREADY_EXISTS.happened();
		}
		else
		{
			snapshot.writeSingleText(path, data);
			return View.noContent();
		}
	}
}
