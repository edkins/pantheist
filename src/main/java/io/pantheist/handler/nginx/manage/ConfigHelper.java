package io.pantheist.handler.nginx.manage;

import java.util.List;
import java.util.Map;

interface ConfigHelper
{
	void write();

	String absolutePath();

	List<ConfigHelperServer> serverList();

	Map<Integer, ConfigHelperServer> servers();

	ConfigHelperServer createLocalServer(int port);

	boolean isEmpty();

	void set(String key, String value);

	void setHttp(String key, String value);

	void setType(String mimeType, String extension);

	void createEventsSection();
}
