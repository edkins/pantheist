package io.pantheist.system.initializer;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class SystemInitializerModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(Initializer.class);
		bind(Initializer.class).to(InitializerImpl.class).in(Scopes.SINGLETON);
		bind(ResourceLoader.class).to(ResourceLoaderImpl.class).in(Scopes.SINGLETON);
	}

}
