package restless.handler.filesystem.backend;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import restless.common.util.Make;

final class FsBoundPathsImpl implements FsBoundPaths
{
	private final boolean bound;
	private final ImmutableMap<FsPathSegment, FsBoundPaths> children;

	FsBoundPathsImpl(
			@JsonProperty("children") final Map<String, FsBoundPaths> children,
			@JsonProperty("bound") final boolean bound)
	{
		this(bound, Make.keysOutOfStrings(children, FsPathSegmentImpl::fromString));
	}

	FsBoundPathsImpl(final boolean bound,
			final Map<FsPathSegment, FsBoundPaths> children)
	{
		if (bound)
		{
			this.bound = true;
			if (!children.isEmpty())
			{
				throw new IllegalArgumentException("Children must be empty if this path is bound");
			}
			this.children = ImmutableMap.of();
		}
		else
		{
			this.bound = false;
			this.children = Make.mapWithout(children, FsBoundPaths::isEmpty);
		}
	}

	@Override
	public Map<FsPathSegment, FsBoundPaths> children()
	{
		return children;
	}

	public static FsBoundPaths empty()
	{
		return new FsBoundPathsImpl(false, ImmutableMap.of());
	}

	@Override
	public FsBoundPaths withBoundPath(final FsPath path)
	{
		if (bound)
		{
			throw new IllegalStateException("Path is already bound");
		}
		else if (path.isEmpty())
		{
			if (isEmpty())
			{
				return new FsBoundPathsImpl(true, ImmutableMap.of());
			}
			else
			{
				throw new IllegalStateException("Cannot bind this because a descendant is bound");
			}
		}
		else
		{
			final FsPathSegment head = path.head();
			final FsPath tail = path.tail();

			final FsBoundPaths childObj = at(head).withBoundPath(tail);
			return new FsBoundPathsImpl(false, Make.overrideMapIfPresent(children, head, childObj));
		}

	}

	@Override
	public boolean isEmpty()
	{
		return !bound && children.isEmpty();
	}

	@Override
	public boolean bound()
	{
		return bound;
	}

	@Override
	public FsBoundPaths at(final FsPathSegment seg)
	{
		if (bound)
		{
			throw new IllegalStateException("Cannot call at() on bound path");
		}
		else if (children.containsKey(seg))
		{
			return children.get(seg);
		}
		else
		{
			return empty();
		}
	}

	@Override
	public boolean canAccommodate(final FsPath path)
	{
		if (bound)
		{
			return false;
		}
		else if (path.isEmpty())
		{
			return isEmpty();
		}
		else
		{
			final FsPathSegment head = path.head();
			final FsPath tail = path.tail();
			return at(head).canAccommodate(tail);
		}
	}

	@Override
	public FsBoundPaths withoutPath(final FsPath path)
	{
		if (bound)
		{
			if (path.isEmpty())
			{
				return empty();
			}
			else
			{
				throw new IllegalStateException("Path not explicitly bound here but parent is");
			}
		}
		else
		{
			if (path.isEmpty())
			{
				throw new IllegalStateException("Cannot remove path because it is not bound");
			}
			else
			{
				final FsPathSegment head = path.head();
				final FsPath tail = path.tail();

				final FsBoundPaths childObj = at(head).withoutPath(tail);
				return new FsBoundPathsImpl(false, Make.overrideMap(children, head, childObj));
			}
		}
	}

}
