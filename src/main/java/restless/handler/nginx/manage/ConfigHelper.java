package restless.handler.nginx.manage;

import java.util.List;
import java.util.Map;

interface ConfigHelper
{
	void write();

	String absolutePath();

	List<ConfigHelperServer> serverList();

	Map<Integer, ConfigHelperServer> servers();
}
