package io.pantheist.system.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

@VisibleForTesting
public class PantheistConfigImpl implements PantheistConfig
{
	private static final Logger LOGGER = LogManager.getLogger(PantheistConfigImpl.class);
	private final ObjectMapper objectMapper;
	private final ArgumentProcessor argumentProcessor;

	// State
	boolean loaded;
	private PantheistConfigFile configFile;

	@Inject
	protected PantheistConfigImpl(final ObjectMapper objectMapper, final ArgumentProcessor argumentProcessor)
	{
		this.objectMapper = checkNotNull(objectMapper);
		this.argumentProcessor = checkNotNull(argumentProcessor);
		this.loaded = false;
		this.configFile = null;
	}

	private <T> Optional<T> property(final Function<PantheistConfigFile, T> getter)
	{
		if (!loaded)
		{
			try
			{
				final Optional<File> file = argumentProcessor.getConfigFile();
				if (file.isPresent())
				{
					configFile = objectMapper.readValue(file.get(), PantheistConfigFile.class);
				}
				else
				{
					LOGGER.info("No config file specified. Using default config.");
				}
			}
			catch (final IOException e)
			{
				Throwables.propagate(e);
			}
			loaded = true;
		}

		if (configFile == null)
		{
			return Optional.empty();
		}
		else
		{
			return Optional.ofNullable(getter.apply(configFile));
		}
	}

	@Override
	public int internalPort()
	{
		return property(PantheistConfigFile::internalPort).orElse(3301);
	}

	@Override
	public File dataDir()
	{
		return property(PantheistConfigFile::dataDir)
				.map(path -> new File(path))
				.orElseGet(() -> new File(System.getProperty("user.dir"), "data"));
	}

	@Override
	public int nginxPort()
	{
		return property(PantheistConfigFile::nginxPort).orElse(3142);
	}

	@Override
	public String nginxExecutable()
	{
		return property(PantheistConfigFile::nginxExecutable).orElse("/usr/sbin/nginx");
	}

	private String relativeToDataDir(final String path)
	{
		final String absolutePath = new File(path).getAbsolutePath();
		final String dataPath = dataDir().getAbsolutePath();
		final String dataPathSlash = dataDir().getAbsolutePath() + "/";

		if (absolutePath.equals(dataPath))
		{
			return "";
		}
		else if (absolutePath.startsWith(dataPathSlash))
		{
			return absolutePath.substring(dataPathSlash.length(), absolutePath.length());
		}
		else
		{
			throw new IllegalArgumentException("Path does not lie inside data dir: " + absolutePath);
		}
	}

	@Override
	public String relativeSystemPath()
	{
		return property(PantheistConfigFile::systemDir)
				.map(this::relativeToDataDir)
				.orElse("system");
	}

	@Override
	public String relativeSrvPath()
	{
		return property(PantheistConfigFile::srvDir)
				.map(this::relativeToDataDir)
				.orElse("srv");
	}

}
