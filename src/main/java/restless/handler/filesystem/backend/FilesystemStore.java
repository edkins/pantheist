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
	FsPath nonemptyPath(String path);

	/**
	 * Bind a filesystem path that hasn't already been bound
	 *
	 * The path must not be empty, must not already be in use and various other
	 * caveats might also apply.
	 *
	 * @param path
	 */
	void bindPath(FsPath path);

	LockedFile lock(FsPath path);

	<T> LockedTypedFile<T> lockJson(FsPath path, Class<T> clazz);

	/**
	 * Look up a particular file and return its management interface.
	 */
	ManagementFunctions manage(FsPath path);
}
