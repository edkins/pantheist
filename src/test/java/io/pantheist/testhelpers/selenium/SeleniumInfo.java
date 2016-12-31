package io.pantheist.testhelpers.selenium;

import org.openqa.selenium.WebDriver;

public interface SeleniumInfo
{
	/**
	 * @return web driver for this session
	 * @throws UnsupportedOperationException
	 *             if selenium is not enabled.
	 */
	WebDriver webDriver();

	boolean useSelenium();

	boolean screenshotOnFailure();
}
