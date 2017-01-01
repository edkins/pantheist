package io.pantheist.handler.kind.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerKindModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(KindModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(Kind.class, KindImpl.class)
				.implement(KindSchema.class, KindSchemaImpl.class)
				.implement(Entity.class, EntityImpl.class)
				.build(KindModelFactory.class));
	}

}
