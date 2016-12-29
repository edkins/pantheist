package restless.api.kind.backend;

import restless.api.kind.model.ApiComponent;
import restless.api.kind.model.ApiEntity;
import restless.api.kind.model.ListComponentResponse;
import restless.common.util.Possible;
import restless.handler.kind.model.Kind;

public interface KindBackend
{
	Possible<Void> putApiEntity(String entityId, ApiEntity entity);

	Possible<ApiEntity> getApiEntity(String entityId);

	Possible<ApiComponent> getComponent(String entityId, String componentId);

	Possible<ListComponentResponse> listComponents(String entityId);

	Possible<Kind> getKind(String kindId);

	Possible<Void> putKind(String kindId, Kind kind);
}
