package io.pantheist.handler.nginx.parser;

public interface NginxSyntax
{
	NginxBlock parse(String text);
}
