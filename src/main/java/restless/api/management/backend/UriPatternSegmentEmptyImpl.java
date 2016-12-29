package restless.api.management.backend;

import java.util.Map;
import java.util.Optional;

final class UriPatternSegmentEmptyImpl implements UriPatternSegment
{
	static UriPatternSegment EMPTY = new UriPatternSegmentEmptyImpl();

	private UriPatternSegmentEmptyImpl()
	{
	}

	@Override
	public boolean matches(final String segment)
	{
		return segment.isEmpty();
	}

	@Override
	public Optional<String> name()
	{
		return Optional.empty();
	}

	@Override
	public String generate(final Map<String, String> values)
	{
		return "";
	}

	@Override
	public String toString()
	{
		return "{{empty}}";
	}
}
