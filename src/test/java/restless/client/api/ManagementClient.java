package restless.client.api;

import restless.client.impl.TargetWrapper;

public interface ManagementClient
{
	/**
	 * @return the root path to the management api
	 */
	ManagementPath manage();

	/**
	 * @return the root path to the main api
	 */
	TargetWrapper main();
}
