package restless.system.config;

import java.io.File;

public interface RestlessConfig
{
	int managementPort();

	/**
	 * @return the directory where all the files get put.
	 */
	File dataDir();
}
