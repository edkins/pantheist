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

import restless.api.management.resource.ManagementResource;
import restless.common.util.MutableOptional;
import restless.handler.binding.backend.BindingStore;
import restless.system.config.RestlessConfig;

final class RestlessServerImpl implements RestlessServer
{
	private static final Logger LOGGER = LogManager.getLogger(RestlessServerImpl.class);
	private final RestlessConfig config;
	private final ManagementResource managementResource;
	private final BindingStore initializer;

	// State
	MutableOptional<Server> serverOpt;

	@Inject
	RestlessServerImpl(final RestlessConfig config,
			final ManagementResource managementResource,
			final BindingStore initializer)
	{
		this.config = checkNotNull(config);
		this.managementResource = checkNotNull(managementResource);
		this.serverOpt = MutableOptional.empty();
		this.initializer = initializer;
	}

	@Override
	public void start()
	{
		initializer.initialize();

		try
		{
			final int port = config.managementPort();

			final ServletContextHandler context = new ServletContextHandler();
			context.setContextPath("/");

			final Server server = new Server(port);
			serverOpt.add(server);
			server.setHandler(context);

			final ResourceConfig resourceConfig = new ResourceConfig();
			resourceConfig.register(managementResource);

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
