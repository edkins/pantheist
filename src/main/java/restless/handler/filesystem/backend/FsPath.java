package restless.handler.filesystem.backend;

import java.io.File;
import java.util.List;

import restless.handler.binding.model.PathSpec;

public interface FsPath
{
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

	FsPath plusRelativePathSpec(PathSpec relativePathSpec);
}
