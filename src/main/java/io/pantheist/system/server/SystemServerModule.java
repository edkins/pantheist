package io.pantheist.system.server;

import java.io.IOException;

import org.eclipse.jetty.server.Server;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

public class SystemServerModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(RestlessServer.class);
		bind(RestlessServer.class).to(RestlessServerImpl.class).in(Scopes.SINGLETON);
	}

	@Provides
	Server provideJettyServer() throws IOException
	{
		return new Server();
	}
}
