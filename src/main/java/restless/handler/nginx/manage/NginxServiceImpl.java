package restless.handler.nginx.manage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.nginx.model.NginxConfig;
import restless.handler.nginx.model.NginxLocation;
import restless.handler.nginx.model.NginxModelFactory;
import restless.handler.nginx.model.NginxServer;
import restless.system.config.RestlessConfig;

final class NginxServiceImpl implements NginxService
{
	private static final Logger LOGGER = LogManager.getLogger(NginxServiceImpl.class);
	private final RestlessConfig config;
	private final NginxModelFactory modelFactory;
	private final FilesystemStore filesystemStore;

	// State
	private boolean running;

	@Inject
	NginxServiceImpl(final RestlessConfig config,
			final NginxModelFactory modelFactory,
			final FilesystemStore filesystemStore)
	{
		this.config = checkNotNull(config);
		this.modelFactory = checkNotNull(modelFactory);
		this.filesystemStore = checkNotNull(filesystemStore);
		this.running = false;
	}

	@Override
	public void configureAndStart()
	{
		final NginxConfig nginx = modelFactory.newConfig();

		final File pidFile = new File(systemDir(), "nginx.pid");
		final File errorLog = new File(systemDir(), "nginx-error.log");
		final File accessLog = new File(systemDir(), "nginx-access.log");

		nginx.pid().giveFile(pidFile);
		nginx.error_log().giveFile(errorLog);
		nginx.http().root().giveFile(config.dataDir());
		nginx.http().access_log().giveFile(accessLog);
		final NginxServer server = nginx.http().addServer("127.0.0.1", config.mainPort());
		final NginxLocation location = server.addLocation("/");

		try
		{
			FileUtils.writeStringToFile(nginxConf(), nginx.toString(), StandardCharsets.UTF_8);
			if (running)
			{
				final Process process = new ProcessBuilder(config.nginxExecutable(), "-c",
						nginxConf().getAbsolutePath(), "-s", "hup").start();
				final int exitCode = process.waitFor();
				if (exitCode != 0)
				{
					throw new NginxServiceException("Non-zero exit code trying to tell nginx to reload configuration");
				}
				LOGGER.info("nginx config reloaded");
			}
			else
			{
				running = true;
				new ProcessBuilder(config.nginxExecutable(), "-c", nginxConf().getAbsolutePath()).start();
				LOGGER.info("nginx started on port {}", config.mainPort());
			}
		}
		catch (final IOException | InterruptedException e)
		{
			throw new NginxServiceException(e);
		}
	}

	private File nginxConf()
	{
		return new File(systemDir(), "nginx.conf");
	}

	private File systemDir()
	{
		return new File(config.dataDir(), filesystemStore.systemBucket().toString());
	}

	@Override
	public void stop()
	{
		if (running)
		{
			try
			{
				final Process process = new ProcessBuilder(config.nginxExecutable(),
						"-c", nginxConf().getAbsolutePath(),
						"-s", "quit").start();
				process.waitFor();
				running = false;
				LOGGER.info("nginx stopped");
			}
			catch (final IOException | InterruptedException e)
			{
				LOGGER.catching(e);

				// Suppress them so we don't cause trouble for other things stopping.
			}
		}
	}

}
