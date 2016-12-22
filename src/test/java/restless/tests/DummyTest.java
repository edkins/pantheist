package restless.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import restless.testhelpers.actions.interf.RestlessActions;
import restless.testhelpers.selenium.ApiRule;
import restless.testhelpers.selenium.Interaction;
import restless.testhelpers.session.MainRule;

public class DummyTest
{
	@ClassRule
	public static final ApiRule apiRule = Interaction.api();

	@Rule
	public final MainRule sessionRule = MainRule.forNewTest(apiRule);

	private RestlessActions act;

	@Before
	public void setup()
	{
		act = sessionRule.actions();
	}

	@Test
	public void access_dummyApi() throws Exception
	{
		assertEquals("hello", act.foo());
	}
}
