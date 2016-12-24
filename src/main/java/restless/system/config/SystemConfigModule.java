package restless.system.config;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.netflix.config.DynamicPropertyFactory;

public class SystemConfigModule extends PrivateModule
{
	@Override
	protected void configure()
	{
		expose(RestlessConfig.class);
		bind(RestlessConfig.class).to(RestlessConfigImpl.class).in(Scopes.SINGLETON);
	}

	@Provides
	DynamicPropertyFactory provideDynamicPropertyFactory()
	{
		return DynamicPropertyFactory.getInstance();
	}
}
