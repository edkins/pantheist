package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherCollectors;

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

	@Override
	public Optional<PathSpecMatch> match(final PathSpec path)
	{
		if (segments.size() != path.segments().size())
		{
			return Optional.empty();
		}
		final ImmutableList.Builder<PathSpecMatchSegment> builder = ImmutableList.builder();

		for (int i = 0; i < segments.size(); i++)
		{
			final PathSpecSegment matcher = segments.get(i);
			final PathSpecSegment matched = path.segments().get(i);
			if (!matcher.matches(matched))
			{
				return Optional.empty();
			}
			builder.add(PathSpecMatchSegmentImpl.from(matcher, ImmutableList.of(matched)));
		}
		return Optional.of(new PathSpecMatchImpl(builder.build()));
	}

	@Override
	public String nameHint()
	{
		return segments.stream()
				.map(PathSpecSegment::nameHint)
				.collect(OtherCollectors.join("_"))
				.orElse("root");
	}

}
