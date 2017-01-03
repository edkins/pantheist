package io.pantheist.handler.nginx.parser;

import java.util.List;

public interface NginxDirective
{
	StringBuilder toStringBuilder(StringBuilder sb);

	String name();

	List<String> parameters();

	void setSingleParameter(String value);

	NginxBlock contents();

	String nlIndent();

	void padTo(String nlIndent);
}
