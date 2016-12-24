package restless.client.api;

public interface ManagementConfig
{
	/**
	 * Binds this particular resource to somewhere in the filesystem. Currently
	 * you don't get to choose where it will go.
	 */
	void bindToFilesystem();

	/**
	 * Binds this resource to one of the resource files.
	 *
	 * @param relativePath subset of resource files to expose, or "" if you want them all.
	 */
	void bindToResourceFiles(String relativePath);
}
