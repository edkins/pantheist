package restless.testhelpers.session;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import restless.common.util.MutableOptional;
import restless.testhelpers.selenium.SeleniumInfo;

public final class TestSessionImpl implements TestSession
{
	private final PortFinder managementPort;

	private final PortFinder mainPort;

	private final SeleniumInfo seleniumInfo;

	private final MutableOptional<File> dataDir;

	private final ObjectMapper objectMapper;

	private TestSessionImpl(final SeleniumInfo seleniumInfo)
	{
		this.managementPort = PortFinder.empty();
		this.mainPort = PortFinder.empty();
		this.seleniumInfo = checkNotNull(seleniumInfo);
		this.dataDir = MutableOptional.empty();
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
		try
		{
			return new URL("http://localhost:" + managementPort.get());
		}
		catch (final MalformedURLException e)
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void supplyDataDir(final File newDataDir)
	{
		dataDir.add(newDataDir);
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

}
