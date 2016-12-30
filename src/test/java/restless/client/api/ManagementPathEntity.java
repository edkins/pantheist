package restless.client.api;

import java.util.List;

import javax.annotation.Nullable;

import restless.api.kind.model.ApiComponent;
import restless.api.kind.model.ApiEntity;
import restless.api.management.model.ListClassifierResponse;

public interface ManagementPathEntity
{
	void putEntity(@Nullable String kindUrl, @Nullable String jsonSchemaUrl, @Nullable String javaUrl);

	ResponseType putEntityResponseType(boolean discovered, @Nullable String kindUrl, @Nullable String jsonSchemaUrl,
			@Nullable String javaUrl);

	ApiEntity getEntity();

	ResponseType getEntityResponseType();

	List<String> listComponentIds();

	ApiComponent getComponent(String componentId);

	ResponseType getComponentResponseType(String componentId);

	ListClassifierResponse listClassifiers();

	ResponseType listClassifierResponseType();

	String urlOfService(String classifierSegment);

}
