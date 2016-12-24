package restless.handler.filesystem.backend;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import restless.common.util.Make;
import restless.common.util.OtherCollectors;
import restless.common.util.OtherPreconditions;
import restless.handler.binding.model.PathSpecSegment;

final class FsPathImpl implements FsPath
{
	private final ImmutableList<FsPathSegment> segments;

	private FsPathImpl(final List<FsPathSegment> segments)
	{
		this.segments = ImmutableList.copyOf(segments);
	}

	@Override
	public String toString()
	{
		return segments.stream()
				.map(FsPathSegment::toString)
				.reduce(OtherCollectors.join("/"))
				.orElse("");
	}

	public static FsPath empty()
	{
		return new FsPathImpl(ImmutableList.of());
	}

	@JsonCreator
	private static FsPathImpl nonemptyFsPathImpl(final String path)
	{
		final ImmutableList.Builder<FsPathSegment> builder = ImmutableList.builder();
		OtherPreconditions.checkNotNullOrEmpty(path);
		if (path.startsWith("/") || path.endsWith("/"))
		{
			throw new IllegalArgumentException("Path cannot start or end with slash");
		}
		for (final String segment : path.split("\\/"))
		{
			builder.add(FsPathSegmentImpl.fromString(segment));
		}
		final ImmutableList<FsPathSegment> list = builder.build();
		if (list.isEmpty())
		{
			throw new IllegalArgumentException("Path cannot be empty here");
		}
		return new FsPathImpl(list);
	}

	@JsonCreator
	public static FsPath nonempty(final String path)
	{
		return nonemptyFsPathImpl(path);
	}

	@Override
	public FsPathSegment head()
	{
		if (segments.isEmpty())
		{
			throw new IllegalStateException("tail: empty path");
		}
		return segments.get(0);
	}

	@Override
	public FsPath tail()
	{
		if (segments.isEmpty())
		{
			throw new IllegalStateException("tail: empty path");
		}
		return new FsPathImpl(Make.tail(segments));
	}

	@Override
	public File in(final File directory)
	{
		File result = directory;
		for (final FsPathSegment segment : segments)
		{
			result = new File(result, segment.toString());
		}
		return result;
	}

	@Override
	public FsPath segment(final FsPathSegment seg)
	{
		return new FsPathImpl(Make.list(segments, seg));
	}

	@Override
	public FsPath segment(final String seg)
	{
		return segment(FsPathSegmentImpl.fromString(seg));
	}

	@Override
	public boolean isEmpty()
	{
		return segments.isEmpty();
	}

	@Override
	public FsPath parent()
	{
		if (segments.isEmpty())
		{
			throw new IllegalStateException("parent: empty path");
		}
		return new FsPathImpl(Make.init(segments));
	}

	@Override
	public List<FsPathSegment> segments()
	{
		return segments;
	}

	@Override
	public FsPath withPathSegments(final List<PathSpecSegment> segments)
	{
		FsPath result = this;
		for (final PathSpecSegment seg : segments)
		{
			if (seg.literal())
			{
				// Note that some paths won't be allowed here, eg ..
				result = result.segment(seg.literalValue());
			}
			else
			{
				throw new UnsupportedOperationException("Can only construct filesystem paths from literal paths");
			}
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(segments);
	}

	@Override
	public boolean equals(final Object object)
	{
		if (object instanceof FsPathImpl)
		{
			final FsPathImpl that = (FsPathImpl) object;
			return Objects.equal(this.segments, that.segments);
		}
		return false;
	}

	@Override
	public List<FsPath> leadingPortions()
	{
		final ImmutableList.Builder<FsPathSegment> portionBuilder = ImmutableList.builder();
		final ImmutableList.Builder<FsPath> resultBuilder = ImmutableList.builder();

		resultBuilder.add(empty());
		for (final FsPathSegment seg : segments)
		{
			portionBuilder.add(seg);
			resultBuilder.add(new FsPathImpl(portionBuilder.build()));
		}
		return resultBuilder.build();
	}
}
