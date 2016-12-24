package restless.handler.binding.backend;

import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerBindingBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(BindingBackendFactory.class);
		expose(BindingStore.class);
		expose(SchemaValidation.class);

		bind(BindingStore.class).to(BindingStoreImpl.class).in(Scopes.SINGLETON);
		bind(SchemaValidation.class).to(SchemaValidationImpl.class).in(Scopes.SINGLETON);

		install(new FactoryModuleBuilder()
				.implement(BindingSet.class, BindingSetImpl.class)
				.implement(ManagementFunctions.class, EmptyManagementFunctionsImpl.class)
				.build(BindingBackendFactory.class));
	}

	@Provides
	private JsonSchemaFactory providesJsonSchemaFactory()
	{
		return JsonSchemaFactory.byDefault();
	}
}
