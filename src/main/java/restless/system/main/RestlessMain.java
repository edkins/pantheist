package restless.system.main;

import com.google.inject.Guice;

import restless.system.server.RestlessServer;

public class RestlessMain
{
	public static void main(final String[] args)
	{
		Guice.createInjector(new AllRestlessModule()).getInstance(RestlessServer.class).start();
	}
}
