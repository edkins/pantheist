package io.pantheist.testclient.api;

public interface ManagementPathLocation
{
	/**
	 * Binds this particular resource to somewhere in the filesystem. Currently
	 * the files will go in the same place specified by the path.
	 *
	 * @param path to bind
	 */
	void bindToFilesystem();

	/**
	 * Binds this resource to somewhere else on the filesystem
	 *
	 * @param absolutePath Absolute path on the filesystem, starting with slash.
	 */
	void bindToExternalFiles(String absolutePath);

	void delete();

	boolean exists();

	String url();

}
