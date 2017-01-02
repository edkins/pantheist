package io.pantheist.api.management.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Optional;

import io.pantheist.common.util.OtherPreconditions;

final class UriPatternSegmentLiteralImpl implements UriPatternSegment
{
	private final String segment;

	private UriPatternSegmentLiteralImpl(final String segment)
	{
		this.segment = OtherPreconditions.checkNotNullOrEmpty(segment);
	}

	public static UriPatternSegment of(final String segment)
	{
		return new UriPatternSegmentLiteralImpl(segment);
	}

	@Override
	public boolean matches(final String segment)
	{
		checkNotNull(segment);
		return this.segment.equals(segment);
	}

	@Override
	public Optional<String> name()
	{
		return Optional.empty();
	}

	@Override
	public String toString()
	{
		return segment;
	}

	@Override
	public String generateAndDelete(final Map<String, String> values)
	{
		return segment;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}
}
