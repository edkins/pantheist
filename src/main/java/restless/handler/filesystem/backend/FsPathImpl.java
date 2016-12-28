package restless.handler.filesystem.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import restless.common.util.Make;
import restless.common.util.MakeList;
import restless.common.util.OtherCollectors;
import restless.common.util.OtherPreconditions;

final class FsPathImpl implements FsPath
{
	private static final MakeList<FsPathSegment, FsPath> MAKE_FS_PATH = Make.wrappedList(FsPathImpl::fromSegments);
	private final ImmutableList<FsPathSegment> segments;

	private FsPathImpl(final List<FsPathSegment> segments)
	{
		this.segments = ImmutableList.copyOf(segments);
	}

	private static FsPath fromSegments(final List<FsPathSegment> segments)
	{
		return new FsPathImpl(segments);
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
		slashSeparated(path, builder);
		final ImmutableList<FsPathSegment> list = builder.build();
		if (list.isEmpty())
		{
			throw new IllegalStateException("Path cannot be empty here");
		}
		return new FsPathImpl(list);
	}

	private static void slashSeparated(final String path, final ImmutableList.Builder<FsPathSegment> builder)
	{
		OtherPreconditions.checkNotNullOrEmpty(path);
		if (path.startsWith("/") || path.endsWith("/"))
		{
			throw new IllegalArgumentException("Path cannot start or end with slash");
		}
		for (final String segment : path.split("\\/"))
		{
			builder.add(FsPathSegmentImpl.fromString(segment));
		}
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
		return MAKE_FS_PATH.tail().from(segments);
	}

	@Override
	public File in(final File directory)
	{
		return Make.<File>single()
				.<FsPathSegment>snowball(directory, (f, seg) -> new File(f, seg.toString()))
				.from(segments);
	}

	@Override
	public FsPath segment(final FsPathSegment seg)
	{
		return MAKE_FS_PATH.from(segments, seg);
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
		return MAKE_FS_PATH.init().from(segments);
	}

	@Override
	public List<FsPathSegment> segments()
	{
		return segments;
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

	@Override
	public FsPath slashSeparatedSegments(final String relativePath)
	{
		checkNotNull(relativePath);
		if (relativePath.isEmpty())
		{
			return this;
		}
		else
		{
			final ImmutableList.Builder<FsPathSegment> builder = ImmutableList.builder();
			builder.addAll(segments);
			slashSeparated(relativePath, builder);
			return new FsPathImpl(builder.build());
		}
	}
}
