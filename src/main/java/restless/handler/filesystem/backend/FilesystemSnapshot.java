package restless.handler.filesystem.backend;

import restless.common.util.AntiIterator;
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
	 * @throws IllegalStateException if you've already called write().
	 */
	<T> T read(FsPath path, InputSteamProcessor<T> fn);

	String readText(FsPath file);

	<T> T readJson(FsPath path, Class<T> clazz);

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
	 * Returns if checkFileState equals REGULAR_FILE. Won't throw an exception if it's something
	 * weird or a directory.
	 */
	boolean safeIsFile(FsPath path);

	/**
	 * Convenience method.
	 *
	 * @throws FsUnexpectedStateException if parent is not a directory
	 * @throws IllegalArgumentException if path is empty
	 */
	boolean parentDirectoryExists(FsPath path);

	/**
	 * This will record the location of any missing directories leading up to path, which will get created
	 * when you call write().
	 *
	 * This calls isDir() internally, so will throw an exception if any level is blocked by the presence of
	 * a regular file.
	 */
	void willNeedDirectory(FsPath path);

	/**
	 * Calls the supplied function, which is assumed to write to a bunch of files.
	 *
	 * A conflict will be detected if:
	 *
	 * - the file modification time is after the snapshot time, or
	 *
	 * - the file has been observed to exist as part of this snapshot and is now deleted.
	 *
	 * It's also invalid if you try to access a file you haven't previously checked the state of.
	 *
	 * Before running fn, this method will create any missing directories discovered by willNeedDirectory().
	 *
	 * @throws FsConflictException
	 * 		if someone else wrote to or deleted any of the files since the snapshot time
	 * @throws FsIoException
	 *      if the supplied function threw an IOException while writing to a file
	 * @throws IllegalStateException
	 *      if you've already called write on this snapshot, since you're not supposed to do it twice.
	 *      (It's ok to never write though)
	 */
	void write(FileProcessor fn);

	/**
	 * Convenience method for when you only want to write to one file.
	 *
	 * Remember that future writes are forbidden on this snapshot so you can't do a bunch of these.
	 */
	void writeSingle(FsPath path, SingleFileProcessor fn);

	void writeSingleText(FsPath path, String text);

	/**
	 * If this is a file, return it.
	 *
	 * If it's a directory then return it and recurse into all child objects.
	 *
	 * If it's missing then return nothing.
	 */
	AntiIterator<FsPath> recurse(FsPath path);

	/**
	 * Return a sequence of all the immediate children. Includes both files and directories.
	 *
	 * Remember to use checkFileState to distinguish them, not isFile/isDir.
	 *
	 * Returns an empty list if dir is missing. Fails if dir is a regular file.
	 */
	AntiIterator<FsPath> listFilesAndDirectories(FsPath dir);

	/**
	 * Add something that needs doing, that you don't want to keep track of yourself.
	 * They will be processed in the order that you add them.
	 *
	 * {{@link #willNeedDirectory(FsPath)} is a special case of this.
	 *
	 * @throws IllegalStateException if you've already queued up a write task for this path.
	 */
	void incidentalWriteTask(FsPath path, SingleFileProcessor task);

	/**
	 * Convenience helper, returns a SingleFileProcessor that writes the given object as json.
	 */
	<T> SingleFileProcessor jsonWriter(T obj);

	boolean haveIncidentalWriteTask(FsPath path);
}
