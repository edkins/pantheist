package io.pantheist.testclient.api;

import javax.annotation.Nullable;

import io.pantheist.api.entity.model.ApiComponent;
import io.pantheist.api.entity.model.ApiEntity;
import io.pantheist.api.entity.model.ListComponentResponse;
import io.pantheist.common.api.model.ListClassifierResponse;

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
