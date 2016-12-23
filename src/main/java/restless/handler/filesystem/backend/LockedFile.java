package restless.handler.filesystem.backend;

import java.io.InputStream;
import java.io.OutputStream;

import restless.handler.filesystem.except.FsIoException;

/**
 * Represents a locked filesystem path, which may correspond to an actual file,
 * a directory or an empty void. Remember to close it when you're done to
 * release the lock.
 */
public interface LockedFile extends AutoCloseable
{
	/**
	 * @throws FsIoException
	 */
	@Override
	void close();

	/**
	 * Create a file here (if there's not one already) and return an output
	 * stream which writes to it.
	 *
	 * If the file was already present its contents will be replaced with
	 * whatever you write here.
	 *
	 * This will create the chain of directories leading up to this file if
	 * necessary. This is atomic (at least as far as creating a new empty file)
	 * to make sure someone else isn't trying to delete them at the same time.
	 * Once there's an empty file there, the delete operation is blocked because
	 * the directories won't be empty.
	 *
	 * @param data
	 *            New file contents
	 * @throws FsIoException
	 *             if there is a directory here instead, or the file is somehow
	 *             otherwise not writeable
	 */
	OutputStream outputStream();

	/**
	 * @return true if this is a regular file, false if it's missing
	 * @throws FsIoException
	 *             if there is a directory here instead
	 */
	boolean fileExits();

	/**
	 * Returns the contents of the file
	 *
	 * @return file contents
	 * @throws FsIoException
	 *             if the file is missing, is not readable or is not a regular
	 *             file
	 */
	InputStream inputStream();

	/**
	 * Change to viewing the given child item (file or directory). There's no
	 * way to backtrack though without releasing and obtaining a new lock.
	 */
	void enter(FsPathSegment segment);

	/**
	 * If there's a directory here, do nothing.
	 *
	 * If the parent is a directory but there's nothing here, create an empty
	 * directory.
	 *
	 * @throws FsIoException
	 *             if parent directory does not exist, or is a file, or if
	 *             there's a file here instead of a directory.
	 */
	void createDirectoryIfNotPresent();
}
