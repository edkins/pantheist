package io.pantheist.handler.nginx.manage;

import java.util.Optional;

import io.pantheist.handler.nginx.parser.NginxDirective;

public interface ConfigHelperLocation
{
	String location();

	NginxDirective directive();

	void setAlias(Optional<String> alias);
}
