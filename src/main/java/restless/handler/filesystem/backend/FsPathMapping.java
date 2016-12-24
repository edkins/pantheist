package restless.handler.filesystem.backend;

import java.io.File;

/**
 * Maps paths to actual files. Used in the write() operation of FilesystemSnapshot.
 *
 * It can only be used on files you've previously checked the state of within a snapshot.
 */
public interface FsPathMapping
{
	/**
	 * @param path
	 * @return corresponding file
	 * @throws IllegalStateException if this was not a file you've previously checked the state of.
	 */
	File get(FsPath path);
}
