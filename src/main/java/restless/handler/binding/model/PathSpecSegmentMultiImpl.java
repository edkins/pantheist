package restless.handler.binding.model;

import com.google.common.base.Objects;

public class PathSpecSegmentMultiImpl implements PathSpecSegment
{

	@Override
	public PathSpecSegmentType type()
	{
		return PathSpecSegmentType.multi;
	}

	@Override
	public boolean literal()
	{
		return false;
	}

	@Override
	public boolean matches(final PathSpecSegment segment)
	{
		return true;
	}

	@Override
	public boolean fixedNumber()
	{
		return false;
	}

	@Override
	public String nameHint()
	{
		return "XX";
	}

	@Override
	public String literalValue()
	{
		throw new IllegalStateException("multi instead of literal path segment");
	}

	@Override
	public String escapedLiteralValue()
	{
		throw new IllegalStateException("multi instead of literal path segment");
	}

	@Override
	public String toString()
	{
		return "**";
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(PathSpecSegmentType.multi);
	}

	@Override
	public boolean equals(final Object object)
	{
		return object instanceof PathSpecSegmentMultiImpl;
	}

}
