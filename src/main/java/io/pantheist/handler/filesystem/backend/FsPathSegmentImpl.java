package io.pantheist.handler.filesystem.backend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Objects;

import io.pantheist.common.util.OtherPreconditions;

final class FsPathSegmentImpl implements FsPathSegment
{
	private final String segment;

	@JsonCreator
	FsPathSegmentImpl(final String segment)
	{
		OtherPreconditions.checkNotNullOrEmpty(segment);

		// Note this covers the special cases . and ..
		// Even if we start allowing files that start with a dot, we need to exclude those.
		if (segment.equals(".") || segment.equals(".."))
		{
			throw new IllegalArgumentException("Special file names . and .. not allowed in FsPathSegment");
		}
		if (segment.contains("/"))
		{
			throw new IllegalArgumentException("Path segment cannot contain slash");
		}

		this.segment = segment;
	}

	static FsPathSegment fromString(final String segment)
	{
		return new FsPathSegmentImpl(segment);
	}

	@Override
	public String toString()
	{
		return segment;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(segment);
	}

	@Override
	public boolean equals(final Object object)
	{
		if (object instanceof FsPathSegmentImpl)
		{
			final FsPathSegmentImpl that = (FsPathSegmentImpl) object;
			return Objects.equal(this.segment, that.segment);
		}
		return false;
	}
}
