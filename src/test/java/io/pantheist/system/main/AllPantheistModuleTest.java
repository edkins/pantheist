package io.pantheist.system.main;

import org.junit.Test;

import com.google.inject.Guice;

import io.pantheist.system.initializer.Initializer;
import io.pantheist.system.main.AllPantheistModule;

public class AllPantheistModuleTest
{
	@Test
	public void can_inject_initializer() throws Exception
	{
		Guice.createInjector(new AllPantheistModule()).getInstance(Initializer.class);
	}
}
