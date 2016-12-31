package io.pantheist.handler.nginx.parser;

interface NginxBlock
{
	StringBuilder toStringBuilder(StringBuilder sb);

	NginxCollection contents();
}
