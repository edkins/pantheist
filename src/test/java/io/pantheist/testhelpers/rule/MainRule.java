package io.pantheist.testhelpers.rule;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.base.Throwables;

import io.pantheist.testclient.api.ManagementClient;
import io.pantheist.testclient.api.ManagementPathJavaFile;
import io.pantheist.testclient.api.ManagementPathKind;
import io.pantheist.testhelpers.classrule.TestSession;
import io.pantheist.testhelpers.selenium.NavigateToHomeRule;
import io.pantheist.testhelpers.selenium.ScreenshotRule;

public class MainRule implements TestRule
{
	private static final Logger LOGGER = LogManager.getLogger(MainRule.class);
	private final TestSession session;

	private MainRule(final TestSession session)
	{
		this.session = checkNotNull(session);
	}

	private RuleChain createRuleChain()
	{
		return RuleChain
				.outerRule(new ErrorLoggingRule())
				.around(DataDirSetupRule.forTest(session))
				.around(ReloadRule.forTest(session))
				.around(navigateToHomeRule())
				.around(screenshotRule());
	}

	private TestRule navigateToHomeRule()
	{
		if (session.useSelenium())
		{
			return NavigateToHomeRule.forTest(session);
		}
		else
		{
			return new NoRule();
		}
	}

	private TestRule screenshotRule()
	{
		if (session.screenshotOnFailure())
		{
			return ScreenshotRule.forTest(session);
		}
		else
		{
			return new NoRule();
		}

	}

	public static MainRule forNewTest(final TestSession session)
	{
		return new MainRule(session);
	}

	public ManagementClient actions()
	{
		return session.client();
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return createRuleChain().apply(base, description);
	}

	private String randomChars(final int count)
	{
		final Random r = new Random();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count; i++)
		{
			sb.append((char) (r.nextInt(26) + 'a'));
		}
		return sb.toString();
	}

	public File createTempDir()
	{
		final File dir = new File(session.dataDir(), "tempdir-" + randomChars(6));
		assertTrue("Could not create dir", dir.mkdir());
		LOGGER.info("Created a temp directory {}", dir.getAbsolutePath());
		return dir;
	}

	public String resource(final String resourcePath)
	{
		try (InputStream input = MainRule.class.getResourceAsStream(resourcePath))
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

	public ManagementPathJavaFile putJavaResource(final String name)
	{
		return putJavaResource(name, name);
	}

	public ManagementPathJavaFile putJavaResource(final String name, final String resourceName)
	{
		final ManagementPathJavaFile java = actions().manage().javaPackage("io.pantheist.examples").file(name);
		java.data().putResource("/java-example/" + resourceName, "text/plain");
		return java;
	}

	public ManagementPathKind putKindResource(final String name)
	{
		final ManagementPathKind kind = actions().manage().kind(name);
		kind.data().putResource("/kind-schema/" + name, "application/json");
		return kind;
	}

	public int nginxPort()
	{
		return session.nginxPort();
	}
}
