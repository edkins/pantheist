package restless.handler.nginx.parser;

import java.util.List;

public interface NginxDirective
{
	StringBuilder toStringBuilder(StringBuilder sb);

	String name();

	List<String> parameters();

	void setSingleParameter(String value);

	NginxCollection contents();
}
