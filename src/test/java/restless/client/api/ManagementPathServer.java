package restless.client.api;

import java.util.List;

import restless.api.management.model.ListConfigItem;

public interface ManagementPathServer
{

	/**
	 * Look up a location identified by path.
	 */
	ManagementPathLocation location(String path);

	List<ListConfigItem> listLocations();

}
