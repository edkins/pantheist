package io.pantheist.api.management.backend;

import java.util.Map;

public interface UriPattern
{
	UriPattern segment(String segment);

	UriPattern var(String name);

	UriPattern emptySegment();

	UriPattern allowTrailingSlash();

	String getVar(String name, String url);

	String generate(Map<String, String> values);

	/**
	 * Return this as a template, with variables enclosed in braces like so:
	 *
	 * http://localhost:3142/thing/{thingId}
	 */
	String template();
}
