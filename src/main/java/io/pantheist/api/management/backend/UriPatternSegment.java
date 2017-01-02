package io.pantheist.api.management.backend;

import java.util.Map;
import java.util.Optional;

interface UriPatternSegment
{
	boolean matches(String segment);

	Optional<String> name();

	/**
	 * Takes a mutable key/value map.
	 *
	 * If this uses a variable name, it will take that variable out of the map.
	 */
	String generateAndDelete(Map<String, String> values);

	boolean isEmpty();
}
