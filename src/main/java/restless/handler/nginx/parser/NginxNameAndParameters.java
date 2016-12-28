package restless.handler.nginx.parser;

import restless.common.util.MutableListView;

interface NginxNameAndParameters
{
	StringBuilder toStringBuilder(StringBuilder sb);

	String name();

	MutableListView<String> parameters();
}
