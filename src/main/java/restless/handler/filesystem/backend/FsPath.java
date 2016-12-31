package restless.handler.filesystem.backend;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import restless.common.util.AntiIterator;

@JsonDeserialize(as = FsPathImpl.class)
public interface FsPath
{
	@JsonValue
	@Override
	String toString();

	/**
	 * @return the first path segment in this path. It will always exist because
	 *         the path is nonempty.
	 * @throws IllegalStateException
	 *             if the path is empty
	 */
	FsPathSegment head();

	/**
	 * @return ireturn everything but the first segment
	 * @throws IllegalStateException
	 *             if the path is empty
	 */
	FsPath tail();

	/**
	 * Treats this as a relative path within the specified directory, and
	 * returns a path to it.
	 */
	File in(File directory);

	/**
	 * Appends a single (valid and nonempty) segment to this path. Returns it as
	 * a new object.
	 */
	FsPath segment(FsPathSegment seg);

	FsPath segment(String seg);

	/**
	 * Returns whether this path is empty, i.e. representing the root directory.
	 */
	boolean isEmpty();

	/**
	 * Return a path representing the parent directory.
	 *
	 * @throws IllegalStateException
	 *             if the path is empty
	 */
	FsPath parent();

	List<FsPathSegment> segments();

	/**
	 * Return a list of all the leading portions of this path, starting with the
	 * empty path and ending with this path.
	 *
	 * e.g. for "a/b/c" it would return ["","a","a/b","a/b/c"]
	 *
	 * This is useful if you need to recursively create a bunch of directories.
	 */
	List<FsPath> leadingPortions();

	/**
	 * Append zero or more segments separated by slashes. Empty string appends nothing. Leading or trailing slashes
	 * are an error
	 */
	FsPath slashSeparatedSegments(String relativePath);

	/**
	 * Return a list of segments that need to be appended to base in order to get here.
	 *
	 * Returns an empty sequence if this is equal to base.
	 *
	 * @throws IllegalArgumentException if this is not a descendant of base.
	 */
	AntiIterator<String> segmentsRelativeTo(FsPath base);

	/**
	 * Returns the last segment, i.e. the name of the file.
	 *
	 * @throws IllegalStateException if the path is empty
	 */
	String lastSegment();
}
