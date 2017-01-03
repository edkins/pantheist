package io.pantheist.api.management.backend;

import io.pantheist.api.management.model.CreateConfigRequest;
import io.pantheist.api.management.model.ListConfigResponse;
import io.pantheist.api.management.model.ListRootResponse;
import io.pantheist.common.util.Possible;

public interface ManagementBackend
{
	Possible<ListConfigResponse> listLocations(String serverId);

	boolean configExists(String serverId, String locationId);

	Possible<Void> putConfig(String serverId, String locationId, CreateConfigRequest request);

	Possible<Void> putData(String path, String data);

	Possible<String> getData(String path);

	Possible<Void> deleteConfig(String serverId, String locationId);

	ListRootResponse listRoot();

	void reloadConfiguration();

	void scheduleTerminate();

	void regenerateDb();
}
