package io.pantheist.api.entity.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.handler.java.model.JavaComponent;
import io.pantheist.handler.schema.model.SchemaComponent;

public interface ApiEntityModelFactory
{
	ApiEntity entity(
			@Assisted("discovered") boolean discovered,
			@Nullable @Assisted("kindUrl") String kindUrl,
			@Nullable @Assisted("jsonSchemaUrl") String jsonSchemaUrl,
			@Nullable @Assisted("javaUrl") String javaUrl,
			@Assisted("valid") boolean valid,
			@Nullable List<ListClassifierItem> childResources);

	ApiComponent component(
			@Nullable SchemaComponent jsonSchema,
			@Nullable JavaComponent java);

	ListComponentItem listComponentItem(
			@Assisted("url") String url,
			@Assisted("componentId") String componentId);

	ListComponentResponse listComponentResponse(List<ListComponentItem> childResources);

	ListEntityItem listEntityItem(
			@Assisted("url") String url,
			@Assisted("entityId") String entityId,
			@Assisted("discovered") boolean discovered,
			@Assisted("kindUrl") String kindUrl);

	ListEntityResponse listEntityResponse(List<ListEntityItem> childResources);
}
