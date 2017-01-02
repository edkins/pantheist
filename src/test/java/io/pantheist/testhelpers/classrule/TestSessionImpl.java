package io.pantheist.testhelpers.classrule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.pantheist.common.util.MutableOpt;
import io.pantheist.common.util.View;
import io.pantheist.system.initializer.Initializer;
import io.pantheist.system.main.AllPantheistModule;
import io.pantheist.testclient.api.ManagementClient;
import io.pantheist.testclient.impl.ManagementClientImpl;
import io.pantheist.testhelpers.selenium.Interaction;
import io.pantheist.testhelpers.selenium.SeleniumInfo;

public final class TestSessionImpl implements TestRule, TestSession
{
	private static final Logger LOGGER = LogManager.getLogger(TestSessionImpl.class);
	private final PortFinder internalPort;

	private final PortFinder mainPort;

	private final PortFinder postgresPort;

	private final SeleniumInfo seleniumInfo;

	private final ObjectMapper objectMapper;

	private final MutableOpt<ManagementClient> client;

	private boolean started;

	private Initializer initializer;

	private TestSessionImpl(final SeleniumInfo seleniumInfo)
	{
		this.internalPort = PortFinder.empty();
		this.mainPort = PortFinder.empty();
		this.postgresPort = PortFinder.empty();
		this.seleniumInfo = checkNotNull(seleniumInfo);
		this.objectMapper = new ObjectMapper();
		this.client = View.mutableOpt();
		this.started = false;
		this.initializer = null;
	}

	public static TestSessionImpl forApi()
	{
		return new TestSessionImpl(Interaction.api());
	}

	@Override
	public WebDriver webDriver()
	{
		return seleniumInfo.webDriver();
	}

	@Override
	public int internalPort()
	{
		return internalPort.get();
	}

	@Override
	public int postgresPort()
	{
		return postgresPort.get();
	}

	private URL localhostWithPort(final int port)
	{
		try
		{
			return new URL("http://127.0.0.1:" + port);
		}
		catch (final MalformedURLException e)
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public URL mainUrl()
	{
		return localhostWithPort(mainPort.get());
	}

	@Override
	public File dataDir()
	{
		return new File("test-data");
	}

	@Override
	public ObjectMapper objectMapper()
	{
		return objectMapper;
	}

	@Override
	public File dumpFile(final String prefix, final String ext)
	{
		final String date = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss").format(new Date());
		return new File("dump/" + prefix + "-" + date + "." + ext);
	}

	@Override
	public boolean useSelenium()
	{
		return seleniumInfo.useSelenium();
	}

	@Override
	public boolean screenshotOnFailure()
	{
		return seleniumInfo.screenshotOnFailure();
	}

	@Override
	public int nginxPort()
	{
		return mainPort.get();
	}

	@Override
	public File originalDataDir()
	{
		return new File(System.getProperty("user.dir"), "data");
	}

	@Override
	public File configFile()
	{
		return new File(dataDir(), "pantheist.conf");
	}

	public Initializer start() throws IOException
	{
		FileUtils.deleteDirectory(dataDir());
		dataDir().mkdir();
		generateConfigFile();
		this.initializer = createInjector().getInstance(Initializer.class);
		return this.initializer;
	}

	public void stop()
	{
		this.initializer.close();
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return new Statement() {

			@Override
			public void evaluate() throws Throwable
			{
				try (Initializer initializer = start())
				{
					base.evaluate();
				}
				catch (final Throwable t)
				{
					LOGGER.catching(t);
					throw t;
				}
			}
		};
	}

	private Injector createInjector()
	{
		final String[] args = new String[] { "-c", configFile().getAbsolutePath() };
		return Guice.createInjector(new AllPantheistModule(args));
	}

	private void generateConfigFile() throws IOException
	{
		final File configFile = configFile();

		final Map<String, Object> map = new HashMap<>();
		map.put("dataDir", dataDir().getAbsolutePath());
		map.put("nginxPort", nginxPort());
		map.put("internalPort", internalPort());
		map.put("postgresPort", postgresPort());

		objectMapper().writeValue(configFile, map);
	}

	@Override
	public ManagementClient client()
	{
		if (!client.isPresent())
		{
			if (useSelenium())
			{
				throw new UnsupportedOperationException("Selenium-based actions not implemented yet");
			}
			else
			{
				// main url and management url are the same for now.
				// "/" is a proxy pass to the management interface in nginx.conf
				client.supply(ManagementClientImpl.from(mainUrl(), mainUrl(), objectMapper()));
			}
		}
		return client.get();
	}

	@Override
	public boolean ensureStarted()
	{
		final boolean wasStarted = started;
		if (!started)
		{
			started = true;
			initializer.start();
		}
		return wasStarted;
	}
}
