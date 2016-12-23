package restless.handler.binding.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

public class HandlerBindingModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(BindingModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(PathSpec.class, PathSpecImpl.class)
				.implement(PathSpecSegment.class, Names.named("literal"), PathSpecSegmentLiteralImpl.class)
				.implement(PathSpecSegment.class, Names.named("star"), PathSpecSegmentStarImpl.class)
				.implement(PathSpecSegment.class, Names.named("multi"), PathSpecSegmentMultiImpl.class)
				.build(BindingModelFactory.class));
	}

}
