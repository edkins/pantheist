package restless.api.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import restless.handler.java.model.JavaComponent;
import restless.handler.schema.model.SchemaComponent;

public interface ApiKindModelFactory
{
	ApiEntity entity(
			@Assisted("discovered") boolean discovered,
			@Nullable @Assisted("kindUrl") String kindUrl,
			@Nullable @Assisted("jsonSchemaUrl") String jsonSchemaUrl,
			@Nullable @Assisted("javaUrl") String javaUrl,
			@Assisted("valid") boolean valid);

	ApiComponent component(
			@Nullable SchemaComponent jsonSchema,
			@Nullable JavaComponent java);

	ListComponentItem listComponentItem(@Assisted("componentId") String componentId);

	ListComponentResponse listComponentResponse(List<ListComponentItem> childResources);
}
