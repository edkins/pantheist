package io.pantheist.handler.nginx.parser;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface NginxBlock
{
	StringBuilder toStringBuilder(StringBuilder sb);

	NginxDirective getOrCreateSimple(String name);

	NginxDirective getOrCreateBlock(String name);

	NginxDirective addBlock(String name, List<String> parameters);

	List<NginxDirective> getAll(String name);

	boolean deleteAllByName(String name);

	Optional<String> lookup(String name);

	boolean removeIf(Predicate<NginxDirective> predicate);

	boolean isEmpty();

	void padTo(String nlIndent);
}
