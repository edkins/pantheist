package io.pantheist.handler.kind.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class HandlerKindBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(KindStore.class);
		expose(KindValidation.class);
		bind(KindStore.class).to(KindStoreImpl.class).in(Scopes.SINGLETON);
		bind(KindValidation.class).to(KindValidationImpl.class).in(Scopes.SINGLETON);
	}

}
