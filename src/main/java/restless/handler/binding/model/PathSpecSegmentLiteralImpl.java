package restless.handler.binding.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.Escapers;
import restless.common.util.OtherPreconditions;

final class PathSpecSegmentLiteralImpl implements PathSpecSegment
{
	private final String value;

	@Inject
	PathSpecSegmentLiteralImpl(@Assisted @JsonProperty("value") final String value)
	{
		this.value = OtherPreconditions.checkNotNullOrEmpty(value);
	}

	@Override
	public PathSpecSegmentType type()
	{
		return PathSpecSegmentType.literal;
	}

	@JsonProperty("value")
	public String value()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return "+" + value;
	}

	@Override
	public boolean literal()
	{
		return true;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(PathSpecSegmentType.literal, value);
	}

	@Override
	public boolean equals(final Object object)
	{
		if (object instanceof PathSpecSegmentLiteralImpl)
		{
			final PathSpecSegmentLiteralImpl that = (PathSpecSegmentLiteralImpl) object;
			return Objects.equal(this.value, that.value);
		}
		return false;
	}

	@Override
	public boolean matches(final PathSpecSegment segment)
	{
		if (!segment.literal())
		{
			throw new IllegalArgumentException("Can only match against literal paths");
		}
		return value.equals(segment.literalValue());
	}

	@Override
	public boolean fixedNumber()
	{
		return true;
	}

	@Override
	public String nameHint()
	{
		return value.replaceAll("[^a-zA-Z]", "_");
	}

	@Override
	public String escapedLiteralValue()
	{
		return Escapers.url(value);
	}

	@Override
	public String literalValue()
	{
		return value;
	}

}
