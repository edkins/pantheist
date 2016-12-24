package restless.handler.binding.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerBindingBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(BindingStore.class);
		bind(BindingStore.class).to(BindingStoreImpl.class).in(Scopes.SINGLETON);
		expose(BindingBackendFactory.class);
		install(new FactoryModuleBuilder()
				.implement(BindingSet.class, BindingSetImpl.class)
				.build(BindingBackendFactory.class));
	}

}
