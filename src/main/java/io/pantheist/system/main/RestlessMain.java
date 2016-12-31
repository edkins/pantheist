package io.pantheist.system.main;

import com.google.inject.Guice;

import io.pantheist.system.initializer.Initializer;

public class RestlessMain
{
	public static void main(final String[] args)
	{
		Guice.createInjector(new AllRestlessModule()).getInstance(Initializer.class).start();
	}
}
