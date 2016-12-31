package io.pantheist.system.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class ArgumentProcessorImpl implements ArgumentProcessor
{
	private static final Logger LOGGER = LogManager.getLogger(ArgumentProcessorImpl.class);
	private final CmdLineArguments arguments;

	private boolean processed;
	private Optional<File> configFile;

	@Inject
	private ArgumentProcessorImpl(final CmdLineArguments arguments)
	{
		this.arguments = checkNotNull(arguments);
		this.processed = false;
		this.configFile = Optional.empty();
	}

	@Override
	public Optional<File> getConfigFile()
	{
		process();
		return configFile;
	}

	private void process()
	{
		if (!processed)
		{
			final List<String> args = arguments.args();
			if (args.isEmpty())
			{
				// leave file as empty
			}
			else
			{
				if (args.size() != 2 || !args.get(0).equals("-c"))
				{
					fail("Usage: -c configfile");
				}
				final File file = new File(args.get(1));
				if (!file.isFile())
				{
					fail("Config file does not exist: " + file.getAbsolutePath());
				}
				configFile = Optional.of(file);
			}
			processed = true;
		}
	}

	private void fail(final String msg)
	{
		LOGGER.error(msg);
		throw new IllegalArgumentException(msg);
	}

}
