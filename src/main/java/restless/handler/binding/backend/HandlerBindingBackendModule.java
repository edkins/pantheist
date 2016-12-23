package restless.handler.binding.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class HandlerBindingBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(BindingStore.class);
		bind(BindingStore.class).to(BindingStoreImpl.class).in(Scopes.SINGLETON);
	}

}
