package restless.api.entity.backend;

import restless.api.entity.model.ApiComponent;
import restless.api.entity.model.ApiEntity;
import restless.api.entity.model.ListComponentResponse;
import restless.api.entity.model.ListEntityResponse;
import restless.common.util.Possible;

public interface EntityBackend
{
	ListEntityResponse listEntities();

	Possible<Void> putApiEntity(String entityId, ApiEntity entity);

	Possible<ApiEntity> getApiEntity(String entityId);

	Possible<ApiComponent> getComponent(String entityId, String componentId);

	Possible<ListComponentResponse> listComponents(String entityId);
}
