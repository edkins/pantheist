package io.pantheist.testhelpers.selenium;

public class Interaction
{
	private Interaction()
	{
		throw new UnsupportedOperationException();
	}

	public static ApiRule api()
	{
		return new NoSelenium();
	}

	public static ApiRule hidden()
	{
		return new FirefoxRule(false, false);
	}

	public static ApiRule screenshotOnFailure()
	{
		return new FirefoxRule(false, true);
	}

	public static ApiRule visible()
	{
		return new FirefoxRule(true, false);
	}
}
