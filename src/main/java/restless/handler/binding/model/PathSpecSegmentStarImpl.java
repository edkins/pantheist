package restless.handler.binding.model;

import com.google.common.base.Objects;

final class PathSpecSegmentStarImpl implements PathSpecSegment
{

	@Override
	public PathSpecSegmentType type()
	{
		return PathSpecSegmentType.star;
	}

	@Override
	public boolean literal()
	{
		return false;
	}

	@Override
	public boolean matches(final PathSpecSegment segment)
	{
		if (!segment.literal())
		{
			throw new IllegalArgumentException("Can only match against literal paths");
		}
		return true;
	}

	@Override
	public boolean fixedNumber()
	{
		return true;
	}

	@Override
	public String nameHint()
	{
		return "X";
	}

	@Override
	public String escapedLiteralValue()
	{
		throw new IllegalStateException("star instead of literal path segment");
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(PathSpecSegmentType.star);
	}

	@Override
	public boolean equals(final Object object)
	{
		return object instanceof PathSpecSegmentStarImpl;
	}

	@Override
	public String literalValue()
	{
		throw new IllegalStateException("star instead of literal path segment");
	}

	@Override
	public String toString()
	{
		return "*";
	}
}
