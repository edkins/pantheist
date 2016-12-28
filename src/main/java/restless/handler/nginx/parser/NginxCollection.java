package restless.handler.nginx.parser;

import restless.common.util.MutableByKey;
import restless.common.util.MutableListView;
import restless.common.util.OptView;

public interface NginxCollection
{
	MutableByKey<String, NginxDirective> byName();

	MutableListView<NginxDirective> list();

	NginxDirective getOrCreateSimple(String name);

	NginxDirective getOrCreateBlock(String name);

	NginxDirective addBlock(String name);

	/**
	 * Look up a simple directive by name. Returns its value
	 * @throws IllegalStateException if there are multiple directives with this name or they have multiple parameters
	 */
	OptView<String> lookup(String name);
}
