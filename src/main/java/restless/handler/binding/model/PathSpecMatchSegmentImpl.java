package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

final class PathSpecMatchSegmentImpl implements PathSpecMatchSegment
{
	private final PathSpecSegment matcher;
	private final List<PathSpecSegment> matched;

	private PathSpecMatchSegmentImpl(final PathSpecSegment matcher, final List<PathSpecSegment> matched)
	{
		this.matcher = checkNotNull(matcher);
		this.matched = checkNotNull(matched);
	}

	static PathSpecMatchSegment from(final PathSpecSegment matcher, final List<PathSpecSegment> matched)
	{
		return new PathSpecMatchSegmentImpl(matcher, matched);
	}

	@Override
	public PathSpecSegment matcher()
	{
		return matcher;
	}

	@Override
	public List<PathSpecSegment> matched()
	{
		return matched;
	}
}
