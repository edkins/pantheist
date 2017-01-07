package io.pantheist.plugin.interfaces;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.handler.filesystem.backend.FileState;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.filesystem.except.FsUnexpectedStateException;

/**
 * Represents a FilesystemSnapshot where some files may already have been changed.
 *
 * This is for when you want some things to happen in sequence, effectively atomically,
 * but each step isn't aware of exactly what the others are doing. Particularly useful
 * for plugins and hooks.
 *
 * Unlike {@link:FilesystemSnapshot}, this does not let you see the files directly.
 */
public interface AlteredSnapshot
{
	/**
	 * Returns true if it's a regular file, false if it's missing
	 *
	 * @throws FsUnexpectedStateException if it's a directory or other kind of filesystem object
	 */
	boolean isFile(FsPath path);

	/**
	 * Returns true if it's a regular file, false if isn't.
	 */
	boolean safeIsFile(FsPath path);

	/**
	 * As with all the other calls, this may not check the filesystem directly if the file has already
	 * been written to.
	 *
	 * @return DOES_NOT_EXIST, REGULAR_FILE or DIRECTORY as appropriate
	 */
	FileState checkFileState(FsPath path);

	/**
	 * Delete a regular file. Subsequent calls to isFile for this path will return false.
	 *
	 * @throws FsUnexpectedStateException if it was not a regular file.
	 */
	void deleteFile(FsPath path);

	/**
	 * Writes the given string to the given file as utf-8. The file will be created if it was not there previously.
	 *
	 * Subsequent calls to isFile will return true for this path.
	 *
	 * @throws FsUnexpectedStateException if it's a directory or other kind of filesystem object
	 */
	void writeText(FsPath path, String text);

	/**
	 * Write out json using ObjectMapper. Otherwise behaves like {@link #writeText(FsPath, String)}.
	 */
	<T> void writeJson(FsPath path, T data);

	/**
	 * Reads the given file as a utf-8 string. (Either directly from the filesystem, or from the last
	 * call to writeText for the same path).
	 */
	String readText(FsPath path);

	/**
	 * List all the known children of the given path
	 *
	 * Returns an empty list if dir is missing.
	 *
	 * @throws FsUnexpectedStateException if dir is a regular file.
	 */
	AntiIterator<FsPath> listFilesAndDirectories(FsPath dir);

	/**
	 * Returns the directory of interest.
	 *
	 * In the context of a change hook, this is the base filesystem dir where entities of the
	 * relevant kind all reside.
	 */
	FsPath baseDir();

	/**
	 * Read file as json using ObjectMapper.
	 */
	<T> T readJson(FsPath path, Class<T> clazz);
}
