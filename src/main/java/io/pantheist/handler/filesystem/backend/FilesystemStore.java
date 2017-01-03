package io.pantheist.handler.filesystem.backend;

import io.pantheist.common.util.Possible;

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
	 * Used by other handlers to store their stuff.
	 */
	FsPath systemBucket();

	FsPath rootPath();

	FsPath srvBucket();

	FsPath projectBucket();

	Possible<Void> putSrvData(String path, String data);

	Possible<String> getSrvData(String path);
}
