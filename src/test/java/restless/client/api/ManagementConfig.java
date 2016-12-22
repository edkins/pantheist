package restless.client.api;

public interface ManagementConfig
{
	/**
	 * Binds this particular resource to a path in the filesystem.
	 *
	 * dataPath is a slash-separated string, e.g. foo/bar
	 *
	 * The following restrictions apply:
	 *
	 * - you can only bind to parts of the filesystem inside Restless's data
	 * directory (this means dataPath can't start with a slash or ..)
	 *
	 * - you can't bind to the root of the data directory (so dataPath can't be
	 * empty)
	 *
	 * - dataPath can't contain other weird stuff like empty segments, trailing
	 * slashes, . or ..
	 *
	 * - Note this is set up for ext4 on Linux. Other operating systems or
	 * filesystems may introduce other restrictions or case insensitivity which
	 * could cause problems here
	 *
	 * - Also if you create a symlink out of the data directory into somewhere
	 * else then that's your own problem to deal with.
	 *
	 * @param dataPath
	 *            where in the data directory to bind to
	 */
	void bindToFilesystem(String dataPath);
}
