package restless.handler.entity.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerEntityModuleModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(EntityModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(Entity.class, EntityImpl.class)
				.build(EntityModelFactory.class));
	}

}
