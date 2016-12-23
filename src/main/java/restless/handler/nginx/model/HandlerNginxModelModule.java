package restless.handler.nginx.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HandlerNginxModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(NginxModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(NginxConfig.class, NginxConfigImpl.class)
				.build(NginxModelFactory.class));
	}

}
