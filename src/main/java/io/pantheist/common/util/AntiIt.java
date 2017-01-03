package io.pantheist.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

public final class AntiIt
{
	private AntiIt()
	{
		throw new UnsupportedOperationException();
	}

	public static <T> AntiIterator<T> from(final Iterable<T> iterable)
	{
		return consumer -> {
			for (final T x : iterable)
			{
				consumer.accept(x);
			}
		};
	}

	public static <T> AntiIterator<T> fromIterator(final Iterator<T> iterator)
	{
		return consumer -> {
			while (iterator.hasNext())
			{
				consumer.accept(iterator.next());
			}
		};
	}

	public static <T> AntiIterator<T> array(final T[] items)
	{
		return consumer -> {
			for (final T x : items)
			{
				consumer.accept(x);
			}
		};
	}

	public static <T> AntiIterator<T> single(final T item)
	{
		return consumer -> consumer.accept(item);
	}

	public static <T> Optional<T> findOnly(final AntiIterator<T> ait, final Predicate<T> predicate)
	{
		final MutableOpt<T> result = View.mutableOpt();

		ait.forEach(x -> {
			if (predicate.test(x))
			{
				result.supply(x);
			}
		});
		return result.toOptional();
	}

	public static <T> AntiIterator<T> empty()
	{
		return consumer -> {
		};
	}

	/**
	 * A string splitter that behaves predictably.
	 */
	public static AntiIterator<String> split(final char separator, final String str)
	{
		checkNotNull(str);
		return consumer -> {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < str.length(); i++)
			{
				final char ch = str.charAt(i);
				if (ch == separator)
				{
					consumer.accept(sb.toString());
					sb.setLength(0);
				}
				else
				{
					sb.append(ch);
				}
			}
			consumer.accept(sb.toString());
		};
	}
}
