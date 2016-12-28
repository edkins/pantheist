package restless.handler.nginx.manage;

import restless.common.util.MutableByKey;
import restless.common.util.OptView;
import restless.handler.nginx.parser.NginxDirective;

public interface ConfigHelperServer
{
	OptView<Integer> port();

	MutableByKey<String, ConfigHelperLocation> locations();

	NginxDirective directive();

	ConfigHelperLocation getOrCreateLocation(String location);
}
