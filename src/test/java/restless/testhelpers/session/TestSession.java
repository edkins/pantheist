package restless.testhelpers.session;

import java.io.File;
import java.net.URL;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;

import restless.testhelpers.selenium.SeleniumInfo;

public interface TestSession extends SeleniumInfo
{
	void clear();

	void supplyDataDir(File dataDir);

	@Override
	WebDriver webDriver();

	int managementPort();

	int mainPort();

	URL managementUrl();

	URL mainUrl();

	File originalDataDir();

	File dataDir();

	File dumpFile(String prefix, String ext);

	ObjectMapper objectMapper();

}
