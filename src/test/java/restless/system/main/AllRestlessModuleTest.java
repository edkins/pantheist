package restless.system.main;

import org.junit.Test;

import com.google.inject.Guice;

import restless.system.initializer.Initializer;

public class AllRestlessModuleTest
{
	@Test
	public void can_inject_initializer() throws Exception
	{
		Guice.createInjector(new AllRestlessModule()).getInstance(Initializer.class);
	}
}
