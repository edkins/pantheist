package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.Make;
import restless.common.util.OtherCollectors;

final class PathSpecImpl implements PathSpec
{
	private final List<PathSpecSegment> segments;
	private final BindingModelFactory modelFactory;

	@Inject
	private PathSpecImpl(@JacksonInject final BindingModelFactory modelFactory,
			@Assisted @JsonProperty("segments") final List<PathSpecSegment> segments)
	{
		checkNotNull(segments);
		this.segments = ImmutableList.copyOf(segments);
		this.modelFactory = checkNotNull(modelFactory);
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
		return Optional.of(modelFactory.pathSpecMatch(builder.build()));
	}

	@Override
	public String nameHint()
	{
		return segments.stream()
				.map(PathSpecSegment::nameHint)
				.reduce(OtherCollectors.join("_"))
				.orElse("root");
	}

	@Override
	public PathSpecClassification classify()
	{
		if (segments.stream().allMatch(PathSpecSegment::literal))
		{
			return PathSpecClassification.EXACT;
		}
		else if (Make.init(segments).stream().allMatch(PathSpecSegment::literal)
				&& Make.last(segments).type().equals(PathSpecSegmentType.multi))
		{
			return PathSpecClassification.PREFIX;
		}
		else
		{
			return PathSpecClassification.OTHER;
		}
	}

	@Override
	public PathSpec minus(final PathSpecSegment seg)
	{
		if (segments.isEmpty() || !Make.last(segments).equals(seg))
		{
			throw new IllegalArgumentException("Path does not end with the given segment");
		}
		return modelFactory.pathSpec(Make.init(segments));
	}

	@Override
	public String literalString()
	{
		final StringBuilder sb = new StringBuilder("/");
		segments.forEach(seg -> sb.append(seg.escapedLiteralValue()).append('/'));
		return sb.toString();
	}

	@Override
	public PathSpec plus(final PathSpecSegment segment)
	{
		return modelFactory.pathSpec(Make.list(segments, segment));
	}

	@Override
	public String literalStringNoLeadingOrTrailingSlashes()
	{
		return segments.stream()
				.map(PathSpecSegment::escapedLiteralValue)
				.reduce(OtherCollectors.join("/"))
				.orElse("");
	}

}
