package restless.handler.nginx.manage;

import restless.common.util.OptView;
import restless.handler.nginx.parser.NginxDirective;

public interface ConfigHelperLocation
{
	String location();

	NginxDirective directive();

	void setAlias(OptView<String> alias);
}
