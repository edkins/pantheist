package io.pantheist.api.management.backend;

import java.util.Map;
import java.util.Optional;

import io.pantheist.common.util.OtherPreconditions;

final class UriPatternSegmentVarImpl implements UriPatternSegment
{
	private final String name;

	private UriPatternSegmentVarImpl(final String name)
	{
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
	}

	public static UriPatternSegment named(final String name)
	{
		return new UriPatternSegmentVarImpl(name);
	}

	@Override
	public boolean matches(final String segment)
	{
		return true;
	}

	@Override
	public Optional<String> name()
	{
		return Optional.of(name);
	}

	@Override
	public String toString()
	{
		return "{" + name + "}";
	}

	@Override
	public String generate(final Map<String, String> values)
	{
		if (values.containsKey(name))
		{
			final String result = values.get(name);
			OtherPreconditions.checkNotNullOrEmpty(result);
			return result;
		}
		else
		{
			throw new IllegalStateException("Name not found in uri pattern values: " + name);
		}
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

}
