package restless.system.config;

import java.io.File;

public interface RestlessConfig
{
	int managementPort();

	int mainPort();

	/**
	 * @return the directory where all the files get put.
	 */
	File dataDir();

	String nginxExecutable();
}
