package io.pantheist.system.main;

import com.google.inject.Guice;

import io.pantheist.system.initializer.Initializer;

public class PantheistMain
{
	public static void main(final String[] args)
	{
		Guice.createInjector(new AllPantheistModule(args)).getInstance(Initializer.class).start();
	}
}
