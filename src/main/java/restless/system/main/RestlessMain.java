package restless.system.main;

import com.google.inject.Guice;

import restless.glue.initializer.Initializer;

public class RestlessMain
{
	public static void main(final String[] args)
	{
		Guice.createInjector(new AllRestlessModule()).getInstance(Initializer.class).start();
	}
}
