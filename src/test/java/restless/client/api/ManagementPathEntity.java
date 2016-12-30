package restless.client.api;

import javax.annotation.Nullable;

import restless.api.entity.model.ApiComponent;
import restless.api.entity.model.ApiEntity;
import restless.api.entity.model.ListComponentResponse;
import restless.common.api.model.ListClassifierResponse;

public interface ManagementPathEntity
{
	void putEntity(@Nullable String kindUrl, @Nullable String jsonSchemaUrl, @Nullable String javaUrl);

	ResponseType putEntityResponseType(boolean discovered, @Nullable String kindUrl, @Nullable String jsonSchemaUrl,
			@Nullable String javaUrl);

	ApiEntity getEntity();

	ResponseType getEntityResponseType();

	ListComponentResponse listComponents();

	ApiComponent getComponent(String componentId);

	ResponseType getComponentResponseType(String componentId);

	ListClassifierResponse listClassifiers();

	ResponseType listClassifierResponseType();

	String urlOfService(String classifierSegment);

	String urlOfComponent(String componentId);

	String url();

}
