package restless.client.api;

public interface ManagementConfig
{
	/**
	 * Binds this particular resource to somewhere in the filesystem. Currently
	 * you don't get to choose where it will go.
	 */
	void bindToFilesystem();
}
