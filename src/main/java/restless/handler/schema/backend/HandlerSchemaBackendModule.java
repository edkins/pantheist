package restless.handler.schema.backend;

import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerSchemaBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(JsonSchemaStore.class);
		bind(JsonSchemaStore.class).to(JsonSchemaStoreImpl.class).in(Scopes.SINGLETON);

		expose(SchemaBackendFactory.class);
		install(new FactoryModuleBuilder()
				.implement(Validator.class, ValidatorJsonImpl.class)
				.build(SchemaBackendFactory.class));
	}

	@Provides
	private JsonSchemaFactory providesJsonSchemaFactory()
	{
		return JsonSchemaFactory.byDefault();
	}

}
