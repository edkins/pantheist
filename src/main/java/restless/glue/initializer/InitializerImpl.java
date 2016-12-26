package restless.glue.initializer;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import restless.common.util.Cleanup;
import restless.glue.nginx.filesystem.NginxFilesystemGlue;
import restless.handler.binding.backend.BindingStore;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.nginx.manage.NginxService;
import restless.system.config.RestlessConfig;
import restless.system.server.RestlessServer;

final class InitializerImpl implements Initializer
{
	private static final Logger LOGGER = LogManager.getLogger(InitializerImpl.class);
	private final FilesystemStore filesystem;
	private final BindingStore bindingStore;
	private final NginxService nginxService;
	private final NginxFilesystemGlue nginxFilesystemGlue;
	private final RestlessServer server;
	private final RestlessConfig config;

	@Inject
	private InitializerImpl(final FilesystemStore filesystem,
			final BindingStore bindingStore,
			final NginxService nginxService,
			final NginxFilesystemGlue nginxFilesystemGlue,
			final RestlessServer server,
			final RestlessConfig config)
	{
		this.filesystem = checkNotNull(filesystem);
		this.bindingStore = checkNotNull(bindingStore);
		this.nginxService = checkNotNull(nginxService);
		this.nginxFilesystemGlue = checkNotNull(nginxFilesystemGlue);
		this.server = checkNotNull(server);
		this.config = checkNotNull(config);
	}

	@Override
	public void start()
	{
		try
		{
			LOGGER.info("Data dir is {}", config.dataDir());

			filesystem.initialize();
			bindingStore.initialize();
			server.start();
			nginxFilesystemGlue.startStopOrRestart();
		}
		catch (final RuntimeException ex)
		{
			LOGGER.warn("Startup failed! Attempting to shut down all of the things.");
			close();
			throw ex;
		}
	}

	@Override
	public void close()
	{
		Cleanup.run(nginxService::stop, server::stop);
	}

}
