package restless.handler.java.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerJavaModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(JavaModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(JavaComponent.class, JavaComponentImpl.class)
				.implement(JavaFileId.class, JavaFileIdImpl.class)
				.implement(JavaBinding.class, JavaBindingImpl.class)
				.build(JavaModelFactory.class));
	}

}
