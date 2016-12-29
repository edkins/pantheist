package restless.common.util;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

/**
 * An iterator which calls you
 */
public interface AntiIterator<T>
{
	void feed(Consumer<T> consumer);

	/**
	 * Concatenate AntiIterators.
	 */
	default AntiIterator<T> andThen(final AntiIterator<T> other)
	{
		return consumer -> {
			feed(consumer);
			other.feed(consumer);
		};
	}

	/**
	 * Return a pair containing each item and the next.
	 *
	 * Includes an item at the start where the first is missing, and an item at the end where the
	 * second is missing.
	 *
	 * So a,b,c would yield (_,a),(a,b),(b,c),(c,_)
	 *
	 * As a special case, an empty sequence will yield a single item (_,_)
	 */
	default AntiIterator<Pair<Optional<T>, Optional<T>>> pairs()
	{
		final MutableOpt<T> prev = View.mutableOpt();
		return consumer -> {
			feed(x -> {
				consumer.accept(Pair.of(prev.toOptional(), Optional.of(x)));
				prev.setSingle(x);
			});
			consumer.accept(Pair.of(prev.toOptional(), Optional.empty()));
		};
	}

	default AntiIterator<T> filter(final Predicate<T> predicate)
	{
		return consumer -> {
			feed(x -> {
				if (predicate.test(x))
				{
					consumer.accept(x);
				}
			});
		};
	}

	default <U> AntiIterator<U> map(final Function<T, U> fn)
	{
		return consumer -> {
			feed(x -> consumer.accept(fn.apply(x)));
		};
	}

	/**
	 * Drop some elements from the start.
	 *
	 * If strict is true, fails if the sequence was shorter than the number specified.
	 */
	default AntiIterator<T> drop(final int count, final boolean strict)
	{
		OtherPreconditions.checkNonNegative(count);
		final AtomicInteger index = new AtomicInteger(0);
		return consumer -> {
			feed(x -> {
				if (index.getAndIncrement() >= count)
				{
					consumer.accept(x);
				}
			});
			if (strict && index.get() < count)
			{
				throw new IllegalStateException("drop: not enough elements");
			}
		};
	}

	/**
	 * Drop the last element
	 */
	default AntiIterator<T> init()
	{
		return pairs()
				.filter(ab -> {
					if (!ab.first().isPresent() && !ab.second().isPresent())
					{
						throw new IllegalStateException("init: empty sequence");
					}
					return ab.first().isPresent() && ab.second().isPresent();
				})
				.map(ab -> ab.first().get());
	}

	/**
	 * Analogous to {@link Stream#reduce(BinaryOperator)}
	 */
	default Optional<T> reduce(final BinaryOperator<T> operator)
	{
		final MutableOpt<T> result = View.mutableOpt();
		feed(x -> {
			if (result.isPresent())
			{
				result.setSingle(operator.apply(result.get(), x));
			}
			else
			{
				result.setSingle(x);
			}
		});
		return result.toOptional();
	}

	/**
	 * Collect the results into a list.
	 */
	default List<T> toList()
	{
		final ImmutableList.Builder<T> builder = ImmutableList.builder();

		feed(builder::add);
		return builder.build();
	}

	default <R> R snowball(final R initial, final BiFunction<R, T, R> accumulate)
	{
		final AtomicReference<R> item = new AtomicReference<>(initial);
		feed(x -> {
			item.set(accumulate.apply(item.get(), x));
		});
		return item.get();
	}

	/**
	 * Collect into an Optional, and fail if there are multiple elements.
	 */
	default Optional<T> failIfMultiple()
	{
		final MutableOpt<T> result = View.mutableOpt();
		feed(result::supply);
		return result.toOptional();
	}

	default <U> AntiIterator<U> flatMap(final Function<T, AntiIterator<U>> fn)
	{
		return consumer -> {
			feed(x -> {
				fn.apply(x).feed(consumer);
			});
		};
	}
}
