package io.pantheist.testhelpers.rule;

import org.openqa.selenium.WebDriver;

public interface WebDriverConsumer
{
	void supplyWebDriver(WebDriver webDriver);
}
