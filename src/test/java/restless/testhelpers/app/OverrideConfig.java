package restless.testhelpers.app;

import javax.inject.Inject;

import com.netflix.config.DynamicPropertyFactory;

import restless.system.config.RestlessConfigImpl;

final class OverrideConfig extends RestlessConfigImpl
{
	private final int testManagementPort;

	@Inject
	OverrideConfig(final DynamicPropertyFactory propertyFactory, @TestManagementPort final int testManagementPort)
	{
		super(propertyFactory);
		this.testManagementPort = testManagementPort;
	}

	@Override
	public int managementPort()
	{
		return testManagementPort;
	}
}
