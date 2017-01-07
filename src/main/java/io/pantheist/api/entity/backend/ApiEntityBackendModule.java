package io.pantheist.api.entity.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public final class ApiEntityBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(EntityBackend.class);
		bind(EntityBackend.class).to(EntityBackendImpl.class).in(Scopes.SINGLETON);
	}

}
