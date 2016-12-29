package restless.handler.entity.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class HandlerEntityBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(EntityStore.class);
		bind(EntityStore.class).to(EntityStoreImpl.class).in(Scopes.SINGLETON);
	}

}
