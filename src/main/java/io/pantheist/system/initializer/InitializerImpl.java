package io.pantheist.system.initializer;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.pantheist.common.util.Cleanup;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.nginx.manage.NginxService;
import io.pantheist.system.config.PantheistConfig;
import io.pantheist.system.server.PantheistServer;

final class InitializerImpl implements Initializer
{
	private static final Logger LOGGER = LogManager.getLogger(InitializerImpl.class);
	private final FilesystemStore filesystem;
	private final NginxService nginxService;
	private final PantheistServer server;
	private final PantheistConfig config;

	@Inject
	private InitializerImpl(final FilesystemStore filesystem,
			final NginxService nginxService,
			final PantheistServer server,
			final PantheistConfig config)
	{
		this.filesystem = checkNotNull(filesystem);
		this.nginxService = checkNotNull(nginxService);
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
			anonymizeNginxConf();
			server.start();
			nginxService.startOrRestart();
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

	private void anonymizeNginxConf()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		final String hiddenText0 = config.dataDir().getAbsolutePath() + slashed(config.relativeSystemPath());
		final String replacementText0 = "${SYSTEM_DIR}";
		final String hiddenText1 = config.dataDir().getAbsolutePath() + slashed(config.relativeSrvPath());
		final String replacementText1 = "${SRV_DIR}";
		final String hiddenText2 = config.dataDir().getAbsolutePath();
		final String replacementText2 = "${DATA_DIR}";
		final String hiddenText3 = "127.0.0.1:" + config.nginxPort();
		final String replacementText3 = "127.0.0.1:${MAIN_PORT}";
		final String hiddenText4 = "127.0.0.1:" + config.internalPort();
		final String replacementText4 = "127.0.0.1:${MANAGEMENT_PORT}";
		final FsPath nginxConf = filesystem.systemBucket().segment("nginx.conf");
		final String text = snapshot
				.readText(nginxConf)
				.replace(hiddenText0, replacementText0)
				.replace(hiddenText1, replacementText1)
				.replace(hiddenText2, replacementText2)
				.replace(hiddenText2, replacementText2)
				.replace(hiddenText3, replacementText3)
				.replace(hiddenText4, replacementText4);

		final FsPath nginxAnonConf = filesystem.systemBucket().segment("nginx-anon.conf");
		snapshot.isFile(nginxAnonConf);
		snapshot.writeSingleText(nginxAnonConf, text);
	}

	@Override
	public void stopAsync()
	{
		new Thread(() -> {
			this.close();
		}).start();
	}
}
