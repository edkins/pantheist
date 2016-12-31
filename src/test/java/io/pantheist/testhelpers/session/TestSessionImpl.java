package io.pantheist.testhelpers.session;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.inject.Guice;

import io.pantheist.common.util.MutableOpt;
import io.pantheist.common.util.View;
import io.pantheist.system.config.PantheistConfig;
import io.pantheist.system.config.SystemConfigModule;
import io.pantheist.testhelpers.selenium.SeleniumInfo;

public final class TestSessionImpl implements TestSession
{
	private final PortFinder managementPort;

	private final PortFinder mainPort;

	private final SeleniumInfo seleniumInfo;

	private final MutableOpt<File> dataDir;

	private final ObjectMapper objectMapper;

	private TestSessionImpl(final SeleniumInfo seleniumInfo)
	{
		this.managementPort = PortFinder.empty();
		this.mainPort = PortFinder.empty();
		this.seleniumInfo = checkNotNull(seleniumInfo);
		this.dataDir = View.mutableOpt();
		this.objectMapper = new ObjectMapper();
	}

	public static TestSession forNewTest(final SeleniumInfo seleniumInfo)
	{
		return new TestSessionImpl(seleniumInfo);
	}

	@Override
	public void clear()
	{
		managementPort.clear();
		dataDir.clear();
	}

	@Override
	public WebDriver webDriver()
	{
		return seleniumInfo.webDriver();
	}

	@Override
	public int managementPort()
	{
		return managementPort.get();
	}

	@Override
	public URL managementUrl()
	{
		return localhostWithPort(managementPort.get());
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
	public void supplyDataDir(final File newDataDir)
	{
		dataDir.supply(newDataDir);
	}

	@Override
	public File dataDir()
	{
		return dataDir.get();
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
	public int mainPort()
	{
		return mainPort.get();
	}

	@Override
	public File originalDataDir()
	{
		return Guice.createInjector(new SystemConfigModule()).getInstance(PantheistConfig.class).dataDir();
	}

}
