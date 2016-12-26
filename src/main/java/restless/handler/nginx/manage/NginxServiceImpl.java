package restless.handler.nginx.manage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import restless.common.util.MutableOptional;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;
import restless.handler.nginx.model.NginxConfig;
import restless.handler.nginx.model.NginxModelFactory;
import restless.system.config.RestlessConfig;

final class NginxServiceImpl implements NginxService
{
	private static final Logger LOGGER = LogManager.getLogger(NginxServiceImpl.class);
	private final RestlessConfig config;
	private final NginxModelFactory modelFactory;
	private final FilesystemStore filesystemStore;

	// State
	MutableOptional<Process> runningProcess;

	@Inject
	NginxServiceImpl(final RestlessConfig config,
			final NginxModelFactory modelFactory,
			final FilesystemStore filesystemStore)
	{
		this.config = checkNotNull(config);
		this.modelFactory = checkNotNull(modelFactory);
		this.filesystemStore = checkNotNull(filesystemStore);
		this.runningProcess = MutableOptional.empty();
	}

	@Override
	public void configureAndStart(final NginxConfig nginx)
	{
		try
		{
			FileUtils.writeStringToFile(nginxConf(), nginx.toString(), StandardCharsets.UTF_8);

			if (runningProcess.isPresent())
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
				if (pidFileExists())
				{
					LOGGER.info("oops, might be nginx process left over from before. Sending it kill signal.");
					tellNginxToQuit();
					LOGGER.info("pidfile has gone now, we assume it's stopped.");
				}

				for (final int port : nginx.ports())
				{
					willNeedPort(port);
				}

				final Process process = new ProcessBuilder(config.nginxExecutable(), "-c",
						nginxConf().getAbsolutePath()).start();
				runningProcess.add(process);
				LOGGER.info("nginx started on port {} with pid {}", config.mainPort(), getPidOfProcess(process));
			}
		}
		catch (final IOException | InterruptedException e)
		{
			throw new NginxServiceException(e);
		}
	}

	private void willNeedPort(final int port)
	{
		try
		{
			(new ServerSocket(port)).close();
		}
		catch (final IOException e)
		{
			LOGGER.debug(e);
			throw new NginxServiceException("Will need port " + port + " but it's in use");
		}
	}

	private static long getPidOfProcess(final Process p)
	{
		long pid = -1;

		try
		{
			if (p.getClass().getName().equals("java.lang.UNIXProcess"))
			{
				final Field f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getLong(p);
				f.setAccessible(false);
			}
		}
		catch (final RuntimeException | NoSuchFieldException | IllegalAccessException e)
		{
			LOGGER.catching(e);
			pid = -1;
		}
		return pid;
	}

	private boolean pidFileExists()
	{
		return sys("nginx.pid").in(config.dataDir()).isFile();
	}

	private File nginxConf()
	{
		return sys("nginx.conf").in(config.dataDir());
	}

	@Override
	public void stop()
	{
		if (runningProcess.isPresent())
		{
			try
			{
				// Tell it to quit
				tellNginxToQuit();

				// Wait for it to quit
				final int exitCode = runningProcess.get().waitFor();
				if (exitCode == 0)
				{
					LOGGER.info("nginx stopped");
				}
				else
				{
					LOGGER.warn("nginx stopped with exit code " + exitCode);
				}
				runningProcess.clear();
			}
			catch (final IOException | InterruptedException e)
			{
				LOGGER.catching(e);

				// Suppress them so we don't cause trouble for other things stopping.
			}
		}
	}

	private void tellNginxToQuit() throws IOException, InterruptedException
	{
		final Process process = new ProcessBuilder(config.nginxExecutable(),
				"-c", nginxConf().getAbsolutePath(),
				"-s", "quit").start();
		process.waitFor();
		while (pidFileExists())
		{
			Thread.sleep(100);
		}
	}

	@Override
	public NginxConfig newConfig()
	{
		final NginxConfig nginx = modelFactory.newConfig();

		nginx.pid().giveFilePath(sys("nginx.pid"));
		nginx.error_log().giveFilePath(sys("nginx-error.log"));
		nginx.http().access_log().giveFilePath(sys("nginx-access.log"));
		nginx.http().charset().giveValue("utf-8");
		nginx.http().addServer(config.mainPort());
		return nginx;
	}

	private FsPath sys(final String name)
	{
		return filesystemStore.systemBucket().segment(name);
	}

}
