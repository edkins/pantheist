package io.pantheist.api.entity.backend;

import io.pantheist.api.entity.model.ApiComponent;
import io.pantheist.api.entity.model.ApiEntity;
import io.pantheist.api.entity.model.ListComponentResponse;
import io.pantheist.api.entity.model.ListEntityResponse;
import io.pantheist.common.util.Possible;

public interface EntityBackend
{
	ListEntityResponse listEntities();

	Possible<Void> putApiEntity(String entityId, ApiEntity entity);

	Possible<ApiEntity> getApiEntity(String entityId);

	Possible<ApiComponent> getComponent(String entityId, String componentId);

	Possible<ListComponentResponse> listComponents(String entityId);
}
