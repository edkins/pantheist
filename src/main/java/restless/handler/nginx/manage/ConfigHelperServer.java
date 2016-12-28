package restless.handler.nginx.manage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import restless.handler.nginx.parser.NginxDirective;

public interface ConfigHelperServer
{
	Optional<Integer> port();

	List<ConfigHelperLocation> locationList();

	Map<String, ConfigHelperLocation> locations();

	NginxDirective directive();

	ConfigHelperLocation getOrCreateLocation(String location);

	boolean removeLocation(String location);
}
