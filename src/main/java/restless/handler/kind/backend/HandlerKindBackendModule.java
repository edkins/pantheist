package restless.handler.kind.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class HandlerKindBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(KindStore.class);
		bind(KindStore.class).to(KindStoreImpl.class).in(Scopes.SINGLETON);
	}

}
