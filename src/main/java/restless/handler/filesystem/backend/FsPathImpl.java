package restless.handler.filesystem.backend;

import java.io.File;
import java.util.List;

import com.google.common.collect.ImmutableList;

import restless.common.util.Make;
import restless.common.util.OtherPreconditions;
import restless.handler.binding.model.PathSpecSegment;
import restless.handler.binding.model.PathSpecSegmentType;

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
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (final FsPathSegment segment : segments)
		{
			if (!first)
			{
				sb.append('/');
			}
			first = false;
			sb.append(segment);
		}
		return sb.toString();
	}

	public static FsPath empty()
	{
		return new FsPathImpl(ImmutableList.of());
	}

	public static FsPath nonempty(final String path)
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
			if (seg.type().equals(PathSpecSegmentType.literal))
			{
				// Note that some paths won't be allowed here, eg ..
				result = result.segment(seg.value());
			}
			else
			{
				throw new UnsupportedOperationException("Can only construct filesystem paths from literal paths");
			}
		}
		return result;
	}
}
