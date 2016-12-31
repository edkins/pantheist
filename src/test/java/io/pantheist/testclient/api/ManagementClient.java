package io.pantheist.testclient.api;

import io.pantheist.testclient.impl.TargetWrapper;

public interface ManagementClient
{
	ManagementPathRoot manage();

	/**
	 * Convenience method. Equivalent to manage().server(mainPort)
	 *
	 * @return the path for managing the main server
	 */
	ManagementPathServer manageMainServer();

	/**
	 * @return the root path to the main api
	 */
	TargetWrapper main();
}
