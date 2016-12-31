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

	/**
	 * Default is "system".
	 *
	 * @return relative path (e.g. "my-system/stuff") to the system config files.
	 */
	String relativeSystemPath();

	/**
	 * Default is "srv".
	 *
	 * @return relative path (e.g. "my-www/somewhere") to the static files which will get served up.
	 */
	String relativeSrvPath();

	String nginxExecutable();

	/**
	 * List of resource files to copy into system/resource-files
	 */
	List<String> resourceFiles();
}
