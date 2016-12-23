package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class PathSpecSegmentImpl implements PathSpecSegment
{
	private final PathSpecSegmentType type;
	private final String value;

	@Inject
	PathSpecSegmentImpl(@Assisted @JsonProperty("type") final PathSpecSegmentType type,
			@Assisted("value") @JsonProperty("value") final String value)
	{
		this.type = checkNotNull(type);
		this.value = OtherPreconditions.checkNotNullOrEmpty(value);
	}

	@Override
	public PathSpecSegmentType type()
	{
		return type;
	}

	@Override
	public String value()
	{
		return value;
	}

	@Override
	public String toString()
	{
		switch (type) {
		case literal:
			return "+" + value;
		case star:
			return "*";
		default:
			return "???" + type + ":" + value;
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(type, value);
	}

	@Override
	public boolean equals(final Object object)
	{
		if (object instanceof PathSpecSegmentImpl)
		{
			final PathSpecSegmentImpl that = (PathSpecSegmentImpl) object;
			return Objects.equal(this.type, that.type)
					&& Objects.equal(this.value, that.value);
		}
		return false;
	}

	@Override
	public boolean literal()
	{
		return type.equals(PathSpecSegmentType.literal);
	}

	@Override
	public boolean matches(final PathSpecSegment segment)
	{
		if (!segment.literal())
		{
			throw new IllegalArgumentException("Can only match against literal paths");
		}
		switch (type) {
		case literal:
			return value.equals(segment.value());
		case star:
			return true;
		default:
			throw new UnsupportedOperationException("Unknown path segment type " + type);
		}
	}

	@Override
	public boolean fixedNumber()
	{
		switch (type) {
		case literal:
		case star:
			return true;
		default:
			throw new UnsupportedOperationException("Unknown path segment type " + type);
		}
	}

	@Override
	public String nameHint()
	{
		switch (type) {
		case literal:
			return value.replaceAll("[^a-zA-Z]", "_");
		case star:
		default:
			return "X";
		}
	}

}
