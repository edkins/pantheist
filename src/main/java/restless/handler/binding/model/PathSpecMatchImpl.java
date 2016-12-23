package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.stream.Collectors;

final class PathSpecMatchImpl implements PathSpecMatch
{
	private final List<PathSpecMatchSegment> segments;

	PathSpecMatchImpl(final List<PathSpecMatchSegment> segments)
	{
		this.segments = checkNotNull(segments);
	}

	@Override
	public List<PathSpecMatchSegment> segments()
	{
		return segments;
	}

	@Override
	public List<PathSpecSegment> nonLiteralChunk()
	{
		final long badCount = segments.stream()
				.filter(seg -> !seg.matcher().fixedNumber())
				.collect(Collectors.counting());

		if (badCount > 1)
		{
			throw new IllegalStateException(
					"Cannot obtain nonLiteralChunk if there are multiple segments with a non-fixed number of matches");
		}

		return segments.stream()
				.filter(seg -> !seg.matcher().literal())
				.flatMap(seg -> seg.matched().stream())
				.collect(Collectors.toList());
	}

}
