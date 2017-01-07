package io.pantheist.common.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * An iterator which calls you
 *
 * You can only call one method on an AntiIterator; after that it will be drained of items.
 */
public interface AntiIterator<T>
{
	/**
	 * This is what you need to implement in order to produce an AntiIterator.
	 *
	 * It's the only method, so you can use functional notation.
	 *
	 * @param consumer something that gets called once for each element in the sequence.
	 */
	void forEach(Consumer<T> consumer);

	/**
	 * Concatenate AntiIterators.
	 */
	default AntiIterator<T> andThen(final AntiIterator<T> other)
	{
		return consumer -> {
			forEach(consumer);
			other.forEach(consumer);
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
			forEach(x -> {
				consumer.accept(Pair.of(prev.toOptional(), Optional.of(x)));
				prev.replace(x);
			});
			consumer.accept(Pair.of(prev.toOptional(), Optional.empty()));
		};
	}

	default AntiIterator<T> filter(final Predicate<T> predicate)
	{
		return consumer -> {
			forEach(x -> {
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
			forEach(x -> consumer.accept(fn.apply(x)));
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
			forEach(x -> {
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
		forEach(x -> {
			if (result.isPresent())
			{
				result.replace(operator.apply(result.get(), x));
			}
			else
			{
				result.replace(x);
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

		forEach(builder::add);
		return builder.build();
	}

	/**
	 * The mutable version of toList.
	 */
	default ArrayList<T> toArrayList()
	{
		final ArrayList<T> list = new ArrayList<>();

		forEach(list::add);
		return list;
	}

	default <R> R snowball(final R initial, final BiFunction<R, T, R> accumulate)
	{
		final AtomicReference<R> item = new AtomicReference<>(initial);
		forEach(x -> {
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
		forEach(result::supply);
		return result.toOptional();
	}

	default <U> AntiIterator<U> flatMap(final Function<T, AntiIterator<U>> fn)
	{
		return consumer -> {
			forEach(x -> {
				fn.apply(x).forEach(consumer);
			});
		};
	}

	/**
	 * Collects into a list and then wraps using the specified function.
	 */
	default <R> R wrap(final Function<List<T>, R> fn)
	{
		return fn.apply(toList());
	}

	default <U> AntiIterator<U> optMap(final Function<T, Optional<U>> fn)
	{
		return consumer -> {
			forEach(x -> {
				final Optional<U> y = fn.apply(x);
				if (y.isPresent())
				{
					consumer.accept(y.get());
				}
			});
		};
	}

	/**
	 * Returns true if this iterator yielded any items.
	 */
	default boolean foundAny()
	{
		final AtomicBoolean result = new AtomicBoolean(false);
		forEach(x -> result.set(true));
		return result.get();
	}

	default AntiIterator<T> append(final T lastItem)
	{
		return consumer -> {
			forEach(consumer);
			consumer.accept(lastItem);
		};
	}

	default AntiIterator<T> tail()
	{
		return drop(1, true);
	}

	/**
	 * Join the elements with the specified delimiter in between, or return empty if the
	 * sequence was empty.
	 *
	 * This is only intended to be used on sequences of strings. It won't call toString() for you.
	 *
	 * (I don't know of a convenient way to ensure the type safety of this)
	 *
	 * @throws ClassCastException if type parameter T is not String.
	 */
	default Optional<String> join(final String delim)
	{
		final AtomicBoolean first = new AtomicBoolean(true);
		final StringBuilder sb = new StringBuilder();
		forEach(x -> {
			if (!first.get())
			{
				sb.append(delim);
			}
			first.set(false);
			sb.append((String) x);
		});
		if (first.get())
		{
			return Optional.empty();
		}
		else
		{
			return Optional.of(sb.toString());
		}
	}

	/**
	 * Return the element with the highest value according to the given evaluator.
	 *
	 * Returns empty if there are no elements.
	 * If there are multiple matches, it will return the first.
	 */
	default Optional<T> max(final Function<T, Long> evaluator)
	{
		final MutableOpt<T> result = View.mutableOpt();
		final MutableOpt<Long> value = View.mutableOpt();
		forEach(x -> {
			final Long newValue = evaluator.apply(x);
			if (!value.isPresent() || newValue > value.get())
			{
				result.replace(x);
				value.replace(newValue);
			}
		});
		return result.toOptional();
	}

	/**
	 * Collect into a list and then sort according to the given comparator.
	 */
	default List<T> toSortedList(final Comparator<T> comparator)
	{
		final ArrayList<T> list = new ArrayList<>();
		forEach(list::add);
		list.sort(comparator);
		return list;
	}

	/**
	 * Return whether all the elements match the given predicate.
	 * Returns true for an empty sequence.
	 *
	 * If the predicate ever returns false, we won't call it any more after that
	 * but we still need to let the upstream AntiIterator generate all its items,
	 * because that's how AntiIterators work.
	 */
	default boolean allMatch(final Predicate<T> predicate)
	{
		final AtomicBoolean result = new AtomicBoolean(true);
		forEach(x -> {
			if (result.get() && !predicate.test(x))
			{
				result.set(false);
			}
		});
		return result.get();
	}

	default <K> Map<K, T> toMap(final Function<T, K> keyGetter)
	{
		final ImmutableMap.Builder<K, T> builder = ImmutableMap.builder();
		forEach(x -> builder.put(keyGetter.apply(x), x));
		return builder.build();
	}

	/**
	 * Returns this as a FilterableObjectStream, for use with interfaces that are expecting that.
	 *
	 * But the filtering will still be done the dumb way, by reading through everything and discarding the
	 * things we don't need.
	 *
	 * This only works if T is ObjectNode.
	 */
	default FilterableObjectStream toDumbFilterableStream()
	{
		return DumbFilterableObjectStream.of(map(x -> (ObjectNode) x));
	}
}
