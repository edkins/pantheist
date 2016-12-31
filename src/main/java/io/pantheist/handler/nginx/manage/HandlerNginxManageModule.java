package io.pantheist.handler.nginx.manage;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerNginxManageModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(NginxService.class);
		bind(NginxService.class).to(NginxServiceImpl.class).in(Scopes.SINGLETON);

		install(new FactoryModuleBuilder()
				.implement(ConfigHelper.class, ConfigHelperImpl.class)
				.build(ConfigHelperFactory.class));
	}

}
