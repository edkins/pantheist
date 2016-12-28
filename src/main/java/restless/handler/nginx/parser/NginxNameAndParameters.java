package restless.handler.nginx.parser;

import java.util.List;

interface NginxNameAndParameters
{
	StringBuilder toStringBuilder(StringBuilder sb);

	String name();

	List<String> parameters();

	void setSingleParameter(String value);
}
