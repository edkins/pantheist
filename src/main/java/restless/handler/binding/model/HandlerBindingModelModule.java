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
				.implement(Binding.class, BindingImpl.class)
				.implement(BindingMatch.class, BindingMatchImpl.class)
				.implement(Handler.class, Names.named("empty"), HandlerEmptyImpl.class)
				.implement(Handler.class, Names.named("filesystem"), HandlerFilesystemImpl.class)
				.implement(Schema.class, Names.named("empty"), SchemaEmptyImpl.class)
				.implement(Schema.class, Names.named("json"), SchemaJsonImpl.class)
				.build(BindingModelFactory.class));
	}

}
