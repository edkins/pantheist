package restless.api.management.backend;

import java.util.Map;
import java.util.Optional;

import restless.common.util.OtherPreconditions;

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
			return values.get(name);
		}
		else
		{
			throw new IllegalStateException("Name not found in uri pattern values: " + name);
		}
	}

}
