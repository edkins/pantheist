package restless.system.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.common.base.Throwables;

import restless.common.annotations.ResourceTag;
import restless.common.util.MutableOpt;
import restless.common.util.View;
import restless.system.config.RestlessConfig;

final class RestlessServerImpl implements RestlessServer
{
	private static final Logger LOGGER = LogManager.getLogger(RestlessServerImpl.class);
	private final RestlessConfig config;
	private final Set<ResourceTag> resourceSet;

	// State
	MutableOpt<Server> serverOpt;

	@Inject
	RestlessServerImpl(
			final RestlessConfig config,
			final Set<ResourceTag> resourceSet)
	{
		this.serverOpt = View.mutableOpt();
		this.config = checkNotNull(config);
		this.resourceSet = checkNotNull(resourceSet);
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
			serverOpt.supply(server);
			server.setHandler(context);

			final ResourceConfig resourceConfig = new ResourceConfig();
			resourceSet.forEach(resourceConfig::register);

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
	public void stop()
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
