package io.pantheist.handler.nginx.manage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.MutableOpt;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.system.config.PantheistConfig;

final class NginxServiceImpl implements NginxService
{
	private static final Logger LOGGER = LogManager.getLogger(NginxServiceImpl.class);
	private final PantheistConfig config;
	private final FilesystemStore filesystemStore;
	private final ConfigHelperFactory helperFactory;

	// State
	MutableOpt<Process> runningProcess;

	@Inject
	NginxServiceImpl(final PantheistConfig config,
			final FilesystemStore filesystemStore,
			final ConfigHelperFactory helperFactory)
	{
		this.config = checkNotNull(config);
		this.filesystemStore = checkNotNull(filesystemStore);
		this.runningProcess = View.mutableOpt();
		this.helperFactory = checkNotNull(helperFactory);
	}

	@Override
	public void startOrRestart()
	{
		try
		{
			if (runningProcess.isPresent())
			{
				final Process process = new ProcessBuilder(config.nginxExecutable(), "-c",
						nginxConf().getAbsolutePath(), "-s", "reload").start();
				final int exitCode = process.waitFor();
				if (exitCode != 0)
				{
					throw new NginxServiceException("Non-zero exit code trying to tell nginx to reload configuration");
				}
				LOGGER.info("nginx config reloaded");

				// Give it some time to process it.
				Thread.sleep(500);
			}
			else
			{
				if (pidFileExists())
				{
					LOGGER.info("oops, might be nginx process left over from before. Sending it kill signal.");
					tellNginxToQuit();
					LOGGER.info("pidfile has gone now, we assume it's stopped.");
				}

				final ConfigHelper helper = helperFactory.helper();
				helper.servers().keySet().forEach(this::willNeedPort);

				final Process process = new ProcessBuilder(
						config.nginxExecutable(),
						"-c",
						helper.absolutePath()).inheritIO().start();
				runningProcess.supply(process);
				LOGGER.info("nginx started on port {} with pid {}", config.nginxPort(), getPidOfProcess(process));
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
		return pidFile().isFile();
	}

	private File pidFile()
	{
		return sys("nginx.pid").in(config.dataDir());
	}

	private File defaultAccessLog()
	{
		return sys("nginx-access.log").in(config.dataDir());
	}

	private File defaultErrorLog()
	{
		return sys("nginx-error.log").in(config.dataDir());
	}

	private File defaultRoot()
	{
		return filesystemStore.srvBucket().in(config.dataDir());
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

	private FsPath sys(final String name)
	{
		return filesystemStore.systemBucket().segment(name);
	}

	@Override
	public boolean hasLocation(final int port, final String location)
	{
		final ConfigHelperServer server = helperFactory
				.helper()
				.servers()
				.get(port);
		if (server == null)
		{
			return false;
		}
		return server.locations().containsKey(location);
	}

	@Override
	public Possible<Void> deleteLocationAndRestart(final int port, final String location)
	{
		final ConfigHelper helper = helperFactory.helper();
		final ConfigHelperServer server = helper
				.servers()
				.get(port);
		if (server == null)
		{
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}

		if (server.removeLocation(location))
		{
			helper.write();
			startOrRestart();
			return View.noContent();
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<List<String>> listLocations(final int port)
	{
		final ConfigHelperServer server = helperFactory
				.helper()
				.servers()
				.get(port);
		if (server == null)
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
		return View.ok(Lists.transform(server.locationList(), ConfigHelperLocation::location));
	}

	@Override
	public Possible<Void> putAndRestart(final int port, final String location, final Optional<String> alias)
	{
		if (!location.startsWith("/") || !location.endsWith("/"))
		{
			return FailureReason.BAD_LOCATION.happened();
		}
		final ConfigHelper helper = helperFactory.helper();
		final ConfigHelperServer server = helper.servers()
				.get(port);
		if (server == null)
		{
			return FailureReason.PARENT_DOES_NOT_EXIST.happened();
		}
		server.getOrCreateLocation(location).setAlias(alias);
		helper.write();
		startOrRestart();
		return View.noContent();
	}

	@Override
	public void generateConfIfMissing()
	{
		final ConfigHelper helper = helperFactory.helper();
		if (!helper.isEmpty())
		{
			return;
		}

		LOGGER.info("{} did not exist so creating one", helper.absolutePath());

		final ConfigHelperServer server = helper.createLocalServer(config.nginxPort());
		final ConfigHelperLocation root = server.getOrCreateLocation("/");

		root.setProxyPass("http://127.0.0.1:" + config.internalPort());

		server.getOrCreateLocation("/resources/");
		server.getOrCreateLocation("/third-party/");

		// Other miscellaneous options we need to set
		helper.set("pid", pidFile().getAbsolutePath());
		helper.set("error_log", defaultErrorLog().getAbsolutePath());
		helper.setHttp("access_log", defaultAccessLog().getAbsolutePath());
		helper.setHttp("root", defaultRoot().getAbsolutePath() + "/");
		helper.setHttp("charset", "utf-8");
		helper.setType("image/png", "png");
		helper.createEventsSection();

		helper.write();
	}
}
