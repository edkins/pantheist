package io.pantheist.testhelpers.rule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

final class ErrorLoggingRule implements TestRule
{
	private static final Logger LOGGER = LogManager.getLogger(ErrorLoggingRule.class);

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
					LOGGER.catching(t);
					throw t;
				}
			}
		};
	}

}
