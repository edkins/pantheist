package restless.api.management.backend;

import restless.api.management.model.ApiComponent;
import restless.api.management.model.ApiEntity;
import restless.api.management.model.CreateConfigRequest;
import restless.api.management.model.ListComponentResponse;
import restless.api.management.model.ListConfigResponse;
import restless.common.util.Possible;
import restless.handler.kind.model.Kind;

public interface ManagementBackend
{
	Possible<ListConfigResponse> listLocations(String serverId);

	boolean configExists(String serverId, String locationId);

	Possible<Void> putConfig(String serverId, String locationId, CreateConfigRequest request);

	Possible<Void> putData(String path, String data);

	Possible<String> getData(String path);

	Possible<Void> putJsonSchema(String schemaId, String schemaText);

	Possible<String> getJsonSchema(String schemaId);

	Possible<Void> validateAgainstJsonSchema(String schemaId, String text);

	Possible<Void> putJavaFile(String pkg, String file, String data);

	Possible<String> getJavaFile(String pkg, String file);

	Possible<Void> deleteConfig(String serverId, String locationId);

	Possible<Void> putEntity(String entityId, ApiEntity entity);

	Possible<ApiEntity> getEntity(String entityId);

	Possible<ApiComponent> getComponent(String entityId, String componentId);

	Possible<ListComponentResponse> listComponents(String entityId);

	Possible<Kind> getKind(String kindId);

	Possible<Void> putKind(String kindId, Kind kind);
}
