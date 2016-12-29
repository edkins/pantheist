package restless.client.api;

import java.util.List;

import javax.annotation.Nullable;

import restless.api.management.model.ApiComponent;
import restless.api.management.model.ApiEntity;

public interface ManagementPathEntity
{
	void putEntity(@Nullable String jsonSchemaUrl, @Nullable String javaUrl);

	ApiEntity getEntity();

	List<String> listComponentIds();

	ApiComponent getComponent(String componentId);

	ResponseType getComponentResponseType(String componentId);
}
