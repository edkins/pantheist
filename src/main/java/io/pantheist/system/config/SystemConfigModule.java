package io.pantheist.system.config;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.netflix.config.DynamicPropertyFactory;

public class SystemConfigModule extends PrivateModule
{
	@Override
	protected void configure()
	{
		expose(PantheistConfig.class);
		bind(PantheistConfig.class).to(PantheistConfigImpl.class).in(Scopes.SINGLETON);
	}

	@Provides
	DynamicPropertyFactory provideDynamicPropertyFactory()
	{
		return DynamicPropertyFactory.getInstance();
	}
}
