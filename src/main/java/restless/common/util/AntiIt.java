package restless.common.util;

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
}
