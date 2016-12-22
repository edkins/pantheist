package restless.system.config;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class SystemConfigModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(RestlessConfig.class);
		bind(RestlessConfig.class).to(RestlessConfigImpl.class).in(Scopes.SINGLETON);
	}

}
