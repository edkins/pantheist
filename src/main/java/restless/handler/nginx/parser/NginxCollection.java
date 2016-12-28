package restless.handler.nginx.parser;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface NginxCollection
{
	List<NginxDirective> list();

	NginxDirective getOrCreateSimple(String name);

	NginxDirective getOrCreateBlock(String name);

	NginxDirective addBlock(String name, List<String> parameters);

	/**
	 * Look up a simple directive by name. Returns its value
	 * @throws IllegalStateException if there are multiple directives with this name or they have multiple parameters
	 */
	Optional<String> lookup(String name);

	List<NginxDirective> getAll(String name);

	boolean deleteAllByName(String name);

	boolean removeIf(Predicate<NginxDirective> predicate);
}
