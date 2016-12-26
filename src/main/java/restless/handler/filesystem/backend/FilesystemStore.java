package restless.handler.filesystem.backend;

import restless.handler.binding.backend.ManagementFunctions;

public interface FilesystemStore
{
	/**
	 * Called by the system when it starts.
	 */
	void initialize();

	FilesystemSnapshot snapshot();

	/**
	 * For operating on a single json file. Parses the given file as json.
	 * When you're finished you can write out a new value.
	 */
	<T> JsonSnapshot<T> jsonSnapshot(FsPath path, Class<T> clazz);

	/**
	 * Look up a particular file and return its management interface.
	 */
	ManagementFunctions manage(FsPath path);

	/**
	 * Used by other handlers to store their stuff.
	 */
	FsPath systemBucket();

	FsPath rootPath();

	FsPath srvBucket();
}
