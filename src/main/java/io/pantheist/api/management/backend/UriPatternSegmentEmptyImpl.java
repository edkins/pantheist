package io.pantheist.api.management.backend;

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
	public String generateAndDelete(final Map<String, String> values)
	{
		return "";
	}

	@Override
	public String toString()
	{
		return "{{empty}}";
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}
}
