package restless.system.config;

import java.io.File;
import java.util.List;

public interface RestlessConfig
{
	int managementPort();

	int mainPort();

	/**
	 * @return the directory where all the files get put.
	 */
	File dataDir();

	String nginxExecutable();

	/**
	 * List of resource files to copy into system/resource-files
	 */
	List<String> resourceFiles();
}
