package restless.handler.nginx.parser;

import restless.common.util.MutableListView;

public interface NginxDirective
{
	StringBuilder toStringBuilder(StringBuilder sb);

	String name();

	MutableListView<String> parameters();

	NginxCollection contents();
}
