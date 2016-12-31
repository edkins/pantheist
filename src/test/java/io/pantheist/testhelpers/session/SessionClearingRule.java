package io.pantheist.testhelpers.session;

import static com.google.common.base.Preconditions.checkNotNull;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

final class SessionClearingRule implements TestRule
{
	private final TestSession session;

	private SessionClearingRule(final TestSession session)
	{
		this.session = checkNotNull(session);
	}

	static TestRule forTest(final TestSession session)
	{
		return new SessionClearingRule(session);
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
				finally
				{
					session.clear();
				}
			}
		};
	}

}
