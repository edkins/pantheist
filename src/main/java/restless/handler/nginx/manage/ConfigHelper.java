package restless.handler.nginx.manage;

import restless.common.util.MutableByKey;

interface ConfigHelper
{
	void write();

	String absolutePath();

	MutableByKey<Integer, ConfigHelperServer> servers();
}
