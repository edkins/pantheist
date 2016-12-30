package restless.api.management.backend;

import java.util.Map;
import java.util.Optional;

interface UriPatternSegment
{
	boolean matches(String segment);

	Optional<String> name();

	String generate(Map<String, String> values);

	boolean isEmpty();
}
