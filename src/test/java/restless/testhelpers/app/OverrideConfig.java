package restless.testhelpers.app;

import java.io.File;

import javax.inject.Inject;

import com.netflix.config.DynamicPropertyFactory;

import restless.system.config.RestlessConfigImpl;

final class OverrideConfig extends RestlessConfigImpl
{
	private final int testManagementPort;
	private final File testDataDir;

	@Inject
	OverrideConfig(final DynamicPropertyFactory propertyFactory, @TestManagementPort final int testManagementPort,
			@TestDataDir final File testDataDir)
	{
		super(propertyFactory);
		this.testManagementPort = testManagementPort;
		this.testDataDir = testDataDir;
	}

	@Override
	public int managementPort()
	{
		return testManagementPort;
	}

	@Override
	public File dataDir()
	{
		return testDataDir;
	}
}
