package restless.testhelpers.selenium;

import java.io.File;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverCommandExecutor;

import com.google.common.collect.ImmutableMap;

import restless.common.util.MutableOptional;

final class FirefoxRule implements ApiRule
{
	private static final int XVFB_DISPLAY = 99;

	private final boolean visible;

	private final boolean screenshotOnFailure;

	// State
	private final MutableOptional<WebDriver> webDriver;

	FirefoxRule(final boolean visible, final boolean screenshotOnFailure)
	{
		this.visible = visible;
		this.screenshotOnFailure = screenshotOnFailure;
		this.webDriver = MutableOptional.empty();
	}

	private static class FirefoxWithEnvDriver extends RemoteWebDriver
	{
		public FirefoxWithEnvDriver(final Map<String, String> env)
		{
			super(createCommandExecutor(env), new DesiredCapabilities(), new DesiredCapabilities());
		}

		private static final CommandExecutor createCommandExecutor(final Map<String, String> env)
		{
			final GeckoDriverService.Builder builder = new GeckoDriverService.Builder();
			builder.usingPort(0);
			builder.withEnvironment(env);
			builder.usingDriverExecutable(geckoDriver());
			return new DriverCommandExecutor(builder.build());
		}

		private static File geckoDriver()
		{
			return new File(System.getProperty("user.home") + "/bin/geckodriver");
		}
	}

	private Map<String, String> env()
	{
		if (visible)
		{
			return ImmutableMap.of();
		}
		else
		{
			return ImmutableMap.of("DISPLAY", ":" + XVFB_DISPLAY);
		}
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return new Statement() {

			@Override
			public void evaluate() throws Throwable
			{
				final WebDriver webDriver = new FirefoxWithEnvDriver(env());
				try
				{
					FirefoxRule.this.webDriver.add(webDriver);
					base.evaluate();
				}
				finally
				{
					webDriver.quit();
				}
			}
		};
	}

	@Override
	public WebDriver webDriver()
	{
		return webDriver.get();
	}

	@Override
	public boolean useSelenium()
	{
		return true;
	}

	@Override
	public boolean screenshotOnFailure()
	{
		return screenshotOnFailure;
	}

}
