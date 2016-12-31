package io.pantheist.testhelpers.selenium;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

final class NoSelenium implements ApiRule
{
	NoSelenium()
	{

	}

	@Override
	public WebDriver webDriver()
	{
		throw new UnsupportedOperationException("No selenium for this test");
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return base;
	}

	@Override
	public boolean useSelenium()
	{
		return false;
	}

	@Override
	public boolean screenshotOnFailure()
	{
		return false;
	}
}
