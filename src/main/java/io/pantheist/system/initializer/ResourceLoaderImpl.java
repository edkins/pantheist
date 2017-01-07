package io.pantheist.system.initializer;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.pantheist.system.config.PantheistConfig;

final class ResourceLoaderImpl implements ResourceLoader
{
	private static final String TEMPLATE_PORT = "{{PORT}}";
	private static final Logger LOGGER = LogManager.getLogger(ResourceLoaderImpl.class);
	private final PantheistConfig config;

	@Inject
	private ResourceLoaderImpl(final PantheistConfig config)
	{
		this.config = checkNotNull(config);
	}

	@Override
	public void copyResourceFilesIfMissing()
	{
		for (final String resourceName : resourceNames())
		{
			if (!resourceName.isEmpty())
			{
				copyIfMissing(resourceName);
			}
		}
	}

	private void copyIfMissing(final String resourceName)
	{
		final String resourcePath = resourcePath(resourceName);
		final File file = new File(new File(config.dataDir(), config.relativeProjectPath()), resourceName);
		if (!file.isFile())
		{
			LOGGER.info("Copying {}", resourceName);
			try (InputStream input = ResourceLoaderImpl.class.getResourceAsStream(resourcePath))
			{
				if (input == null)
				{
					throw new InitializerException("Resource is missing: " + resourcePath);
				}
				final String text = IOUtils.toString(input, StandardCharsets.UTF_8);
				final String port = String.valueOf(config.nginxPort());
				FileUtils.forceMkdirParent(file);
				final String newText = text.replace(TEMPLATE_PORT, port);
				FileUtils.writeStringToFile(file, newText, StandardCharsets.UTF_8);
			}
			catch (final IOException e)
			{
				throw new InitializerException(e);
			}
		}
	}

	private String resourcePath(String resourceName)
	{
		final int i = resourceName.indexOf('/');
		if (i != -1)
		{
			resourceName = resourceName.substring(0, i).replace("-", "") + resourceName.substring(i);
		}
		return "/io/pantheist/resources/" + resourceName;
	}

	private List<String> resourceNames()
	{
		final String resourcePath = "/io/pantheist/resources/resource-list.txt";
		try (InputStream input = ResourceLoaderImpl.class.getResourceAsStream(resourcePath))
		{
			if (input == null)
			{
				throw new InitializerException("Resource is missing: " + resourcePath);
			}
			return IOUtils.readLines(input, StandardCharsets.UTF_8);
		}
		catch (final IOException e)
		{
			throw new InitializerException(e);
		}
	}

}
