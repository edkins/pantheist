package restless.tests;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;

import com.google.common.base.Throwables;

import restless.client.api.ManagementPath;
import restless.client.impl.TargetWrapper;
import restless.testhelpers.selenium.Interaction;
import restless.testhelpers.session.MainRule;

public abstract class BaseTest
{
	@Rule
	public final MainRule sessionRule = MainRule.forNewTest(Interaction.api());

	protected ManagementPath manage;

	protected TargetWrapper mainApi;

	@Before
	public void setup()
	{
		manage = sessionRule.actions().manage();
		mainApi = sessionRule.actions().main();
	}

	protected String resource(final String resourcePath)
	{
		try (InputStream input = BaseTest.class.getResourceAsStream(resourcePath))
		{
			return IOUtils.toString(input, StandardCharsets.UTF_8);
		}
		catch (final IOException e)
		{
			throw Throwables.propagate(e);
		}
	}

}
