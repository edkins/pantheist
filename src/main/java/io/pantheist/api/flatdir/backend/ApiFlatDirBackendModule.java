package io.pantheist.api.flatdir.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public final class ApiFlatDirBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(FlatDirBackend.class);
		bind(FlatDirBackend.class).to(FlatDirBackendImpl.class).in(Scopes.SINGLETON);
	}

}
