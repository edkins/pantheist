package restless.system.server;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.common.base.Throwables;

import restless.api.something.resource.SomethingResource;
import restless.system.config.RestlessConfig;
import restless.util.MutableOptional;

final class RestlessServerImpl implements RestlessServer
{
	private static final Logger LOGGER = LogManager.getLogger(RestlessServerImpl.class);
	private final RestlessConfig config;
	private final SomethingResource somethingResource;

	// State
	MutableOptional<Server> serverOpt;

	@Inject
	RestlessServerImpl(final RestlessConfig config, final SomethingResource somethingResource)
	{
		this.config = checkNotNull(config);
		this.somethingResource = checkNotNull(somethingResource);
		this.serverOpt = MutableOptional.empty();
	}

	@Override
	public void start()
	{
		try
		{
			final int port = config.managementPort();

			final ServletContextHandler context = new ServletContextHandler();
			context.setContextPath("/");

			final Server server = new Server(port);
			serverOpt.add(server);
			server.setHandler(context);

			final ResourceConfig resourceConfig = new ResourceConfig();
			resourceConfig.register(somethingResource);

			context.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");

			server.start();

			LOGGER.info("Running http server on localhost:{}", port);
		}
		catch (final Exception e)
		{
			throw new StartupException(e);
		}
	}

	@Override
	public void close()
	{
		if (serverOpt.isPresent())
		{
			try
			{
				serverOpt.get().stop();
			}
			catch (final Exception e)
			{
				Throwables.propagate(e);
			}
		}
		serverOpt.clear();
	}

}
