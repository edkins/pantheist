package restless.client.api;

import restless.api.management.model.ListConfigResponse;

public interface ManagementConfig
{
	/**
	 * Issues a post request to the config resource.
	 *
	 * This will create a new configuration point associated with the given path.
	 *
	 * @param path to bind
	 */
	ManagementConfigPoint create(String path);

	/**
	 * List all the configs.
	 */
	ListConfigResponse list();
}
