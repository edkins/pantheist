package io.pantheist.testhelpers.selenium;

import static com.google.common.base.Preconditions.checkNotNull;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.pantheist.testhelpers.session.TestSession;

public class NavigateToHomeRule implements TestRule
{
	private final TestSession session;

	private NavigateToHomeRule(final TestSession session)
	{
		this.session = checkNotNull(session);
	}

	public static TestRule forTest(final TestSession session)
	{
		return new NavigateToHomeRule(session);
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return new Statement() {

			@Override
			public void evaluate() throws Throwable
			{
				session.webDriver().navigate().to(session.mainUrl());
				base.evaluate();
			}
		};
	}
}
