package io.pantheist.system.initializer;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.pantheist.common.util.Cleanup;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.java.backend.JavaStore;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.kind.plugin.KindPlugin;
import io.pantheist.handler.nginx.manage.NginxService;
import io.pantheist.handler.plugin.backend.PluginHandler;
import io.pantheist.handler.sql.backend.SqlService;
import io.pantheist.system.config.PantheistConfig;
import io.pantheist.system.server.PantheistServer;

final class InitializerImpl implements Initializer
{
	private static final Logger LOGGER = LogManager.getLogger(InitializerImpl.class);
	private final FilesystemStore filesystem;
	private final NginxService nginxService;
	private final PantheistServer server;
	private final PantheistConfig config;
	private final SqlService sqlService;
	private final KindStore kindStore;
	private final JavaStore javaStore;
	private final PluginHandler pluginHandler;
	private final ResourceLoader resourceLoader;

	@Inject
	private InitializerImpl(final FilesystemStore filesystem,
			final NginxService nginxService,
			final PantheistServer server,
			final PantheistConfig config,
			final SqlService sqlService,
			final KindStore kindStore,
			final JavaStore javaStore,
			final PluginHandler pluginHandler,
			final ResourceLoader resourceLoader)
	{
		this.filesystem = checkNotNull(filesystem);
		this.nginxService = checkNotNull(nginxService);
		this.server = checkNotNull(server);
		this.config = checkNotNull(config);
		this.sqlService = checkNotNull(sqlService);
		this.kindStore = checkNotNull(kindStore);
		this.javaStore = checkNotNull(javaStore);
		this.pluginHandler = checkNotNull(pluginHandler);
		this.resourceLoader = checkNotNull(resourceLoader);
	}

	@Override
	public void start()
	{
		try
		{
			LOGGER.info("Data dir is {}", config.dataDir());

			filesystem.initialize();
			nginxService.generateConfIfMissing();
			server.start();
			sqlService.startOrRestart();
			nginxService.startOrRestart();
			regenerateDb();
		}
		catch (final RuntimeException ex)
		{
			LOGGER.catching(ex);
			LOGGER.warn("Startup failed! Attempting to shut down all of the things.");
			close();
			throw ex;
		}
	}

	@Override
	public void reload()
	{
		LOGGER.info("Reload");
		nginxService.generateConfIfMissing();
		nginxService.startOrRestart();
		regenerateDb();
	}

	@Override
	public void regenerateDb()
	{
		resourceLoader.copyResourceFilesIfMissing();
		pluginHandler.deregisterAllPlugins();
		pluginHandler.registerPluginClass("kind", KindPlugin.class);
		pluginHandler.sendGlobalChangeSignal();

		sqlService.deleteAllTables(); // all transient data anyway so we can trash it
		kindStore.registerKindsInSql();
		javaStore.registerFilesInSql();
	}

	@Override
	public void close()
	{
		Cleanup.run(nginxService::stop, server::stop, sqlService::stop);
	}

	private String slashed(final String relativePath)
	{
		if (relativePath.isEmpty())
		{
			return "";
		}
		else
		{
			return "/" + relativePath;
		}
	}

	@Override
	public void stopAsync()
	{
		new Thread(() -> {
			this.close();
		}).start();
	}
}
