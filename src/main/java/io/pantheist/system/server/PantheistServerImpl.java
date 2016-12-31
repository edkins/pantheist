package io.pantheist.system.server;

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

import io.pantheist.common.annotations.ResourceTag;
import io.pantheist.common.util.MutableOpt;
import io.pantheist.common.util.View;
import io.pantheist.system.config.PantheistConfig;

final class PantheistServerImpl implements PantheistServer
{
	private static final Logger LOGGER = LogManager.getLogger(PantheistServerImpl.class);
	private final PantheistConfig config;
	private final Set<ResourceTag> resourceSet;

	// State
	MutableOpt<Server> serverOpt;

	@Inject
	PantheistServerImpl(
			final PantheistConfig config,
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
