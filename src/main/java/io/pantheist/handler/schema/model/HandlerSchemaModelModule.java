package io.pantheist.handler.schema.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerSchemaModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(SchemaModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(SchemaComponent.class, SchemaComponentImpl.class)
				.build(SchemaModelFactory.class));
	}

}
