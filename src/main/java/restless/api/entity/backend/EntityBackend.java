package restless.api.entity.backend;

import restless.api.kind.model.ApiComponent;
import restless.api.kind.model.ApiEntity;
import restless.api.kind.model.ListComponentResponse;
import restless.api.kind.model.ListEntityResponse;
import restless.common.util.Possible;

public interface EntityBackend
{
	ListEntityResponse listEntities();

	Possible<Void> putApiEntity(String entityId, ApiEntity entity);

	Possible<ApiEntity> getApiEntity(String entityId);

	Possible<ApiComponent> getComponent(String entityId, String componentId);

	Possible<ListComponentResponse> listComponents(String entityId);
}
