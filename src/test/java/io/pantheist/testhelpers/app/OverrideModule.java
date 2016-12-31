package io.pantheist.testhelpers.app;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.netflix.config.DynamicPropertyFactory;

import io.pantheist.system.config.PantheistConfig;
import io.pantheist.testhelpers.session.TestSession;

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
		expose(PantheistConfig.class);
		bind(PantheistConfig.class).to(OverrideConfig.class).in(Scopes.SINGLETON);
		bind(Integer.class).annotatedWith(TestManagementPort.class).toInstance(session.managementPort());
		bind(Integer.class).annotatedWith(TestMainPort.class).toInstance(session.mainPort());
		bind(File.class).annotatedWith(TestDataDir.class).toInstance(session.dataDir());
	}

	@Provides
	DynamicPropertyFactory provideDynamicPropertyFactory()
	{
		return DynamicPropertyFactory.getInstance();
	}

}
