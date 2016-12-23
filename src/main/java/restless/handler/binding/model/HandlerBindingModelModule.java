package restless.handler.binding.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerBindingModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(BindingModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(PathSpec.class, PathSpecImpl.class)
				.implement(PathSpecSegment.class, PathSpecSegmentImpl.class)
				.build(BindingModelFactory.class));
	}

}
