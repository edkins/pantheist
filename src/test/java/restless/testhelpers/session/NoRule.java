package restless.testhelpers.session;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

class NoRule implements TestRule
{

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return base;
	}

}
