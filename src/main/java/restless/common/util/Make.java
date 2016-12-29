package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

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

	public static <T> MakeList<T, List<T>> list()
	{
		return Make.<List<T>>single().through(xs -> xs);
	}

	public static <T, R> MakeList<T, R> wrappedList(final Function<List<T>, R> func)
	{
		return Make.<R>single().through(func);
	}

	public static <T> MakeSingle<T, T> single()
	{
		return MakeSingleImpl.fromFunc(x -> x);
	}

	public static <T> MakeList<T, Optional<T>> failIfMultiple()
	{
		return Make.<Optional<T>>single().fromList(Optional::empty, Optional::of, xs -> {
			throw new IllegalStateException("failIfMultiple: expecting 1 element, got " + xs.size());
		});
	}

	public static <T> MakeList<T, T> theOnly()
	{
		return Make.<T>single().fromList(() -> {
			throw new NoSuchElementException("theOnly: nothing found");
		}, x -> x, xs -> {
			throw new IllegalStateException("theOnly: expecting 1 element, got " + xs.size());
		});
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

	public static MakeList<String, Optional<String>> join(final String delim)
	{
		return Make.<Optional<String>>single()
				.throughAntiIterator(ait -> ait.reduce(OtherCollectors.join(delim)));
	}

	/**
	 * A string splitter that behaves predictably.
	 */
	public static List<String> split(final char separator, final String str)
	{
		checkNotNull(str);
		final ImmutableList.Builder<String> builder = ImmutableList.builder();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++)
		{
			final char ch = str.charAt(i);
			if (ch == separator)
			{
				builder.add(sb.toString());
				sb.setLength(0);
			}
			else
			{
				sb.append(ch);
			}
		}
		builder.add(sb.toString());
		return builder.build();
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
}
