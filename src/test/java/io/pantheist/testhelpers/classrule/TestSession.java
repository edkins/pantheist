package io.pantheist.testhelpers.classrule;

import java.io.File;
import java.net.URL;

import org.openqa.selenium.WebDriver;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.testclient.api.ManagementClient;
import io.pantheist.testhelpers.selenium.SeleniumInfo;

public interface TestSession extends SeleniumInfo
{
	@Override
	WebDriver webDriver();

	int internalPort();

	int nginxPort();

	int postgresPort();

	URL mainUrl();

	File originalDataDir();

	File dataDir();

	File configFile();

	File dumpFile(String prefix, String ext);

	ObjectMapper objectMapper();

	ManagementClient client();

	boolean ensureStarted();
}
