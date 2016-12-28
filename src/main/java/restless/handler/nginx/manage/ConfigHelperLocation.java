package restless.handler.nginx.manage;

import java.util.Optional;

import restless.handler.nginx.parser.NginxDirective;

public interface ConfigHelperLocation
{
	String location();

	NginxDirective directive();

	void setAlias(Optional<String> alias);
}
