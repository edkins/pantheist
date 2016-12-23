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
	public boolean contains(final PathSpecSegment other)
	{
		if (type.equals(PathSpecSegmentType.literal) && other.type().equals(PathSpecSegmentType.literal))
		{
			return value.equals(other.value());
		}
		else
		{
			// We'll handle the other cases when they appear.
			return false;
		}
	}

}
