package io.pantheist.handler.nginx.parser;

public interface NginxSyntax
{
	NginxRoot parse(String text);
}
