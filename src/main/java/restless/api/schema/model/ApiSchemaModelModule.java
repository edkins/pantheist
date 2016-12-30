package restless.api.schema.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ApiSchemaModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ApiSchemaModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ListSchemaItem.class, ListSchemaItemImpl.class)
				.implement(ListSchemaResponse.class, ListSchemaResponseImpl.class)
				.build(ApiSchemaModelFactory.class));
	}

}
