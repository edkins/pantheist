package restless.testhelpers.selenium;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import restless.testhelpers.session.TestSession;

public final class ScreenshotRule implements TestRule
{
	private static final Logger LOGGER = LogManager.getLogger(ScreenshotRule.class);
	private final TestSession session;

	private ScreenshotRule(final TestSession session)
	{
		this.session = checkNotNull(session);
	}

	public static TestRule forTest(final TestSession session)
	{
		return new ScreenshotRule(session);
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return new Statement() {

			@Override
			public void evaluate() throws Throwable
			{
				try
				{
					base.evaluate();
				}
				catch (final Throwable t)
				{
					final TakesScreenshot takesScreenshot = (TakesScreenshot) session.webDriver();
					final File screenshot = takesScreenshot.getScreenshotAs(OutputType.FILE);
					final File dumpFile = session.dumpFile("screenshot", "png");
					FileUtils.copyFile(screenshot, dumpFile);
					LOGGER.info("Saved screenshot as {}", dumpFile);
					throw t;
				}
			}
		};
	}

}
