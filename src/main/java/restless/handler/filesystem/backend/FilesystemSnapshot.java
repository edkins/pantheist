package restless.handler.filesystem.backend;

import java.util.Collection;
import java.util.Map;

import restless.handler.filesystem.except.FsConflictException;
import restless.handler.filesystem.except.FsIoException;
import restless.handler.filesystem.except.FsUnexpectedStateException;

/**
 * Represents the filesystem at a particular point in time.
 *
 * You can atomically read files and receive an exception if they were modified since that time.
 *
 * When you're finished with this snapshot, you can write out all the files in one atomic operation.
 */
public interface FilesystemSnapshot
{
	/**
	 * Attempts to read a file.
	 *
	 * @throws FsConflictException if someone else wrote to the file since the snapshot time.
	 * @throws FsIoException if the supplied function threw an IOException while reading from the file.
	 */
	<T> T read(FsPath path, InputSteamProcessor<T> fn);

	/**
	 * Returns the type of file: regular file, directory or missing.
	 */
	FileState checkFileState(FsPath path);

	/**
	 * Convenience method.
	 *
	 * @throws FsUnexpectedStateException if it's a regular file instead
	 */
	boolean isDir(FsPath path);

	/**
	 * Convenience method.
	 *
	 * @throws FsUnexpectedStateException if it's a directory file instead
	 */
	boolean isFile(FsPath path);

	/**
	 * Convenience method.
	 *
	 * @throws FsUnexpectedStateException if parent is not a directory
	 * @throws IllegalArgumentException if path is empty
	 */
	boolean parentDirectoryExists(FsPath path);

	/**
	 * Writes a bunch of files. Detects conflicts atomically. If any of the operations throws an exception,
	 * it will attempt to execute the remaining operations and will then propagate the exception.
	 *
	 * A conflict will be detected if:
	 *
	 * - the file modification time is after the snapshot time, or
	 *
	 * - the file has been observed to exist as part of this snapshot and is now deleted.
	 *
	 * This applies to both the files included in the map here, and any files that we read from or otherwise accessed.
	 *
	 * @throws FsConflictException
	 * 		if someone else wrote to or deleted any of the files since the snapshot time. If this happens, we
	 *      make sure none of the files will get written.
	 * @throws FsIoException
	 *      if the supplied function threw an IOException while writing to a file
	 * @throws IllegalStateException
	 *      if you've already called write on this snapshot, since you're not supposed to do it twice.
	 *      (It's ok to never write though)
	 */
	void write(Map<FsPath, FileProcessor> fns);

	/**
	 * Even though it looks weird, this version is the most convenient if the files need to
	 * be processed in a particular order, e.g. recursively creating directories.
	 */
	void orderedWrite(Collection<FsPath> paths, PathProcessor fn);
}
