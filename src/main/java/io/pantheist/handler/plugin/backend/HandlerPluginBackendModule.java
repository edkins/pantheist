package io.pantheist.handler.plugin.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public final class HandlerPluginBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(PluginHandler.class);
		bind(PluginHandler.class).to(PluginHandlerImpl.class).in(Scopes.SINGLETON);
	}

}
