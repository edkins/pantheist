package restless.handler.filesystem.backend;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
}
