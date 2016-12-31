package io.pantheist.testhelpers.session;

import java.io.File;
import java.net.URL;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.testhelpers.selenium.SeleniumInfo;

public interface TestSession extends SeleniumInfo
{
	void clear();

	void supplyDataDir(File dataDir);

	void supplyConfigFile(File configFile);

	@Override
	WebDriver webDriver();

	int internalPort();

	int nginxPort();

	URL managementUrl();

	URL mainUrl();

	File originalDataDir();

	File dataDir();

	File configFile();

	File dumpFile(String prefix, String ext);

	ObjectMapper objectMapper();

}
