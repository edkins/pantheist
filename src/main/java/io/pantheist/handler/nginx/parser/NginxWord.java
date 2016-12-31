package io.pantheist.handler.nginx.parser;

public interface NginxWord
{
	StringBuilder toStringBuilder(final StringBuilder sb);

	String value();

	void setValue(String newValue);
}
