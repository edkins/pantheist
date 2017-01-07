package io.pantheist.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * Operations on immutable objects.
 */
public class Make
{
	private Make()
	{
		throw new UnsupportedOperationException();
	}

	public static <T> T last(final List<T> xs)
	{
		checkNotNull(xs);
		if (xs.isEmpty())
		{
			throw new IllegalArgumentException("last: empty list");
		}
		return xs.get(xs.size() - 1);
	}

	/**
	 * Return whether xs starts with ys, or they are equal.
	 *
	 * e.g. returns true for xs=[1,2,3], ys=[1,2]
	 */
	public static <T> boolean listStartsWith(final List<T> xs, final List<T> ys)
	{
		if (xs.size() < ys.size())
		{
			return false;
		}
		for (int i = 0; i < ys.size(); i++)
		{
			if (!xs.get(i).equals(ys.get(i)))
			{
				return false;
			}
		}
		return true;
	}

	@Nullable
	public static <T> ImmutableList<T> copyOfNullable(@Nullable final List<T> list)
	{
		if (list == null)
		{
			return null;
		}
		return ImmutableList.copyOf(list);
	}

	@Nullable
	public static <T> ImmutableList<T> emptyIfNullable(@Nullable final List<T> list)
	{
		if (list == null)
		{
			return ImmutableList.of();
		}
		return ImmutableList.copyOf(list);
	}
}
