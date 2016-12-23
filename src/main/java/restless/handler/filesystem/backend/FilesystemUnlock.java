package restless.handler.filesystem.backend;

interface FilesystemUnlock
{
	/**
	 * Called by LockedFileImpl when it's closed.
	 */
	void unlock(FsPath path);
}
