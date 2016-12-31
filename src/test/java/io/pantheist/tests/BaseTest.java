package io.pantheist.tests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;

import com.google.common.base.Throwables;

import io.pantheist.testclient.api.ManagementPathRoot;
import io.pantheist.testclient.api.ManagementPathServer;
import io.pantheist.testclient.impl.TargetWrapper;
import io.pantheist.testhelpers.selenium.Interaction;
import io.pantheist.testhelpers.session.MainRule;

public abstract class BaseTest
{
	@Rule
	public final MainRule mainRule = MainRule.forNewTest(Interaction.api());

	protected TargetWrapper mainApi;

	protected ManagementPathRoot manage;

	protected ManagementPathServer mmain;

	@Before
	public void setup()
	{
		manage = mainRule.actions().manage();
		mmain = mainRule.actions().manageMainServer();
		mainApi = mainRule.actions().main();
	}

	protected String resource(final String resourcePath)
	{
		try (InputStream input = BaseTest.class.getResourceAsStream(resourcePath))
		{
			if (input == null)
			{
				throw new IllegalArgumentException("Resource does not exist: " + resourcePath);
			}
			return IOUtils.toString(input, StandardCharsets.UTF_8);
		}
		catch (final IOException e)
		{
			throw Throwables.propagate(e);
		}
	}

}
