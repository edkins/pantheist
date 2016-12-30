package restless.api.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import restless.handler.java.model.JavaComponent;
import restless.handler.kind.model.JavaClause;
import restless.handler.kind.model.KindLevel;
import restless.handler.schema.model.SchemaComponent;
import restless.handler.uri.ListClassifierItem;

public interface ApiKindModelFactory
{
	ApiKind kind(
			@Nullable List<ListClassifierItem> childResources,
			@Nullable @Assisted("kindId") String kindId,
			KindLevel level,
			@Assisted("discoverable") Boolean discoverable,
			@Nullable JavaClause java);

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

	ListComponentItem listComponentItem(@Assisted("componentId") String componentId);

	ListComponentResponse listComponentResponse(List<ListComponentItem> childResources);

	ListEntityItem listEntityItem(@Assisted("entityId") String entityId, @Assisted("discovered") boolean discovered);

	ListEntityResponse listEntityResponse(List<ListEntityItem> childResources);
}
