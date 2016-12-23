package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

final class PathSpecImpl implements PathSpec
{
	private final List<PathSpecSegment> segments;

	@Inject
	private PathSpecImpl(@Assisted @JsonProperty("segments") final List<PathSpecSegment> segments)
	{
		checkNotNull(segments);
		this.segments = ImmutableList.copyOf(segments);
	}

	@Override
	public List<PathSpecSegment> segments()
	{
		return segments;
	}

	@Override
	public boolean contains(final PathSpec other)
	{
		for (int i = 0; i < segments.size(); i++)
		{
			if (i >= other.segments().size() || !segments.get(i).contains(other.segments().get(i)))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public PathSpec relativeTo(final PathSpec other)
	{
		final ImmutableList.Builder<PathSpecSegment> builder = ImmutableList.builder();

		if (other.segments().size() > segments.size())
		{
			throw new IllegalArgumentException("This does not start with other because other is longer");
		}

		for (int i = 0; i < segments.size(); i++)
		{
			if (i < other.segments().size())
			{
				if (!other.segments().get(i).equals(segments.get(i)))
				{
					throw new IllegalArgumentException("This does not start with other");
				}
			}
			else
			{
				builder.add(segments.get(i));
			}
		}

		return new PathSpecImpl(builder.build());
	}

	@Override
	public String toString()
	{
		if (segments.isEmpty())
		{
			return "/";
		}
		else
		{
			final StringBuilder sb = new StringBuilder();
			for (final PathSpecSegment seg : segments)
			{
				sb.append('/').append(seg);
			}
			return sb.toString();
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(segments);
	}

	@Override
	public boolean equals(final Object object)
	{
		if (object instanceof PathSpecImpl)
		{
			final PathSpecImpl that = (PathSpecImpl) object;
			return Objects.equal(this.segments, that.segments);
		}
		return false;
	}

}
