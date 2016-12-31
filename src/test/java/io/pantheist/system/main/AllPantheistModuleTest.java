package io.pantheist.system.main;

import org.junit.Test;

import com.google.inject.Guice;

import io.pantheist.system.initializer.Initializer;

public class AllPantheistModuleTest
{
	@Test
	public void can_inject_initializer() throws Exception
	{
		final String[] args = new String[0];
		Guice.createInjector(new AllPantheistModule(args)).getInstance(Initializer.class);
	}
}
