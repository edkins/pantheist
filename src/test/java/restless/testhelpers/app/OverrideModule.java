package restless.testhelpers.app;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.netflix.config.DynamicPropertyFactory;

import restless.system.config.RestlessConfig;
import restless.testhelpers.session.TestSession;

class OverrideModule extends PrivateModule
{
	private final TestSession session;

	OverrideModule(final TestSession session)
	{
		this.session = checkNotNull(session);
	}

	@Override
	protected void configure()
	{
		expose(RestlessConfig.class);
		bind(RestlessConfig.class).to(OverrideConfig.class).in(Scopes.SINGLETON);
		bind(Integer.class).annotatedWith(TestManagementPort.class).toInstance(session.managementPort());
	}

	@Provides
	DynamicPropertyFactory provideDynamicPropertyFactory()
	{
		return DynamicPropertyFactory.getInstance();
	}

}
