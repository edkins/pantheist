package restless.system.config;

import java.io.File;

public interface RestlessConfig
{
	int managementPort();

	/**
	 * @return the directory where all the files get put.
	 */
	File dataDir();

	/**
	 * @return subdirectory where the filesystem handler's own metadata is
	 *         stored
	 */
	String fsFilesystemPath();

	/**
	 * @return subdirectory where binding information is stored
	 */
	String fsBindingPath();
}
