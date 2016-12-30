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

import restless.api.java.resource.JavaResource;
import restless.api.kind.resource.KindResource;
import restless.api.management.resource.ManagementResource;
import restless.api.schema.resource.SchemaResource;
import restless.common.util.MutableOpt;
import restless.common.util.View;
import restless.system.config.RestlessConfig;

final class RestlessServerImpl implements RestlessServer
{
	private static final Logger LOGGER = LogManager.getLogger(RestlessServerImpl.class);
	private final RestlessConfig config;
	private final ManagementResource managementResource;
	private final KindResource kindResource;
	private final JavaResource javaResource;
	private final SchemaResource schemaResource;

	// State
	MutableOpt<Server> serverOpt;

	@Inject
	RestlessServerImpl(
			final RestlessConfig config,
			final ManagementResource managementResource,
			final KindResource kindResource,
			final JavaResource javaResource,
			final SchemaResource schemaResource)
	{
		this.serverOpt = View.mutableOpt();
		this.config = checkNotNull(config);
		this.managementResource = checkNotNull(managementResource);
		this.kindResource = checkNotNull(kindResource);
		this.javaResource = checkNotNull(javaResource);
		this.schemaResource = checkNotNull(schemaResource);
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
			resourceConfig
					.register(managementResource)
					.register(kindResource)
					.register(javaResource)
					.register(schemaResource);

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
