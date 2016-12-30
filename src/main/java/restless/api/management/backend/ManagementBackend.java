package restless.api.management.backend;

import restless.api.management.model.CreateConfigRequest;
import restless.api.management.model.ListClassifierResponse;
import restless.api.management.model.ListConfigResponse;
import restless.common.util.Possible;

public interface ManagementBackend
{
	Possible<ListConfigResponse> listLocations(String serverId);

	boolean configExists(String serverId, String locationId);

	Possible<Void> putConfig(String serverId, String locationId, CreateConfigRequest request);

	Possible<Void> putData(String path, String data);

	Possible<String> getData(String path);

	Possible<Void> deleteConfig(String serverId, String locationId);

	ListClassifierResponse listRootClassifiers();

	void reloadConfiguration();

	void scheduleTerminate();
}
