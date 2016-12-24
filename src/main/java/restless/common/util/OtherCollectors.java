package restless.common.util;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

public final class OtherCollectors
{
	private OtherCollectors()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Join streams of strings using the given delimiter.
	 *
	 * This is actually used with reduce() not collect().
	 *
	 * Returns an Optional&lt;String&gt; which will be empty if the stream was
	 * empty.
	 */
	public static BinaryOperator<String> join(final String delim)
	{
		return (a, b) -> a + delim + b;
	}

	public static <T> Collector<T, MutableOptional<T>, Optional<T>> toOptional()
	{
		return new Collector<T, MutableOptional<T>, Optional<T>>() {

			@Override
			public BiConsumer<MutableOptional<T>, T> accumulator()
			{
				return (a, x) -> a.add(x);
			}

			@Override
			public Set<java.util.stream.Collector.Characteristics> characteristics()
			{
				return ImmutableSet.of();
			}

			@Override
			public BinaryOperator<MutableOptional<T>> combiner()
			{
				return (a, b) -> {
					a.add(b);
					return a;
				};
			}

			@Override
			public Function<MutableOptional<T>, Optional<T>> finisher()
			{
				return MutableOptional::value;
			}

			@Override
			public Supplier<MutableOptional<T>> supplier()
			{
				return MutableOptional::empty;
			}
		};
	}

	public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList.Builder<T>> toListBuilder()
	{
		return new Collector<T, ImmutableList.Builder<T>, ImmutableList.Builder<T>>() {

			@Override
			public BiConsumer<Builder<T>, T> accumulator()
			{
				return (a, x) -> a.add(x);
			}

			@Override
			public Set<java.util.stream.Collector.Characteristics> characteristics()
			{
				return ImmutableSet.of();
			}

			@Override
			public BinaryOperator<Builder<T>> combiner()
			{
				return (a, b) -> a.addAll(b.build());
			}

			@Override
			public Function<Builder<T>, Builder<T>> finisher()
			{
				return a -> a;
			}

			@Override
			public Supplier<Builder<T>> supplier()
			{
				return ImmutableList::builder;
			}

		};
	}
}
