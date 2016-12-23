package restless.handler.filesystem.backend;

import restless.handler.binding.backend.ManagementFunctions;

public interface FilesystemStore
{
	/**
	 * Called by the system when it starts.
	 */
	void initialize();

	/**
	 * Turn a string into a FsPath, under the assumption that it's nonempty and
	 * valid.
	 */
	FsPath fromBucketName(String path);

	LockedFile lock(FsPath path);

	<T> LockedTypedFile<T> lockJson(FsPath path, Class<T> clazz);

	/**
	 * Look up a particular file and return its management interface.
	 */
	ManagementFunctions manage(FsPath path);

	/**
	 * Used by other handlers to store their stuff.
	 */
	FsPath systemBucket();

	/**
	 * Create a new directory with a name similar to the one specified, and
	 * return its path.
	 */
	FsPath newBucket(String nameHint);

	FsPath rootPath();
}
