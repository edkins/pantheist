package restless.testhelpers.session;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import restless.client.api.ManagementClient;
import restless.client.impl.ManagementClientImpl;
import restless.testhelpers.app.AppRule;
import restless.testhelpers.app.DataFileImportRule;
import restless.testhelpers.app.TempDirRule;
import restless.testhelpers.app.WaitForServerRule;
import restless.testhelpers.selenium.NavigateToHomeRule;
import restless.testhelpers.selenium.ScreenshotRule;
import restless.testhelpers.selenium.SeleniumInfo;

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
				.outerRule(SessionClearingRule.forTest(session))
				.around(new ErrorLoggingRule())
				.around(TempDirRule.forTest(session))
				.around(DataFileImportRule.forTest(session))
				.around(AppRule.forTest(session))
				.around(WaitForServerRule.forTest(session))
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

	public static MainRule forNewTest(final SeleniumInfo seleniumInfo)
	{
		final TestSession session = TestSessionImpl.forNewTest(seleniumInfo);
		return new MainRule(session);
	}

	public ManagementClient actions()
	{
		if (session.useSelenium())
		{
			throw new UnsupportedOperationException("Selenium-based actions not implemented yet");
		}
		else
		{
			// main url and management url are the same for now.
			// "/" is a proxy pass to the management interface in nginx.conf
			return ManagementClientImpl.from(session.mainUrl(), session.mainUrl(), session.objectMapper());
		}
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
}
