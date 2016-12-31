package io.pantheist.handler.filesystem.backend;

import io.pantheist.handler.filesystem.except.FsParseException;
import io.pantheist.handler.filesystem.except.FsUnexpectedStateException;

public interface JsonSnapshot<T>
{
	/**
	 * Return whether the file exists.
	 */
	boolean exists();

	/**
	 * Obtain the contents of this file.
	 *
	 * @throws FsUnexpectedStateException if the file does not exist.
	 * @throws FsParseException if the file contents were invalid or null
	 */
	T read();

	/**
	 * Write out the new value.
	 *
	 * @throws IllegalArgumentException if it could not write the data
	 * @throws IllegalStateException if already written
	 */
	void write(T value);

	/**
	 * Convenience method. Assuming the original object returned by read() was mutable and you changed it,
	 * will write the new version out. Equivalent to:
	 * @throws IllegalStateException if already written or you did not call get.
	 */
	void writeMutable();
}
