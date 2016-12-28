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

	public static <T> Collector<T, MutableOpt<T>, Optional<T>> toOpt()
	{
		return new Collector<T, MutableOpt<T>, Optional<T>>() {

			@Override
			public BiConsumer<MutableOpt<T>, T> accumulator()
			{
				return (a, x) -> a.supply(x);
			}

			@Override
			public Set<java.util.stream.Collector.Characteristics> characteristics()
			{
				return ImmutableSet.of();
			}

			@Override
			public BinaryOperator<MutableOpt<T>> combiner()
			{
				return (a, b) -> {
					if (b.isPresent())
					{
						a.supply(b.get());
					}
					return a;
				};
			}

			@Override
			public Function<MutableOpt<T>, Optional<T>> finisher()
			{
				return MutableOpt::toOptional;
			}

			@Override
			public Supplier<MutableOpt<T>> supplier()
			{
				return View::mutableOpt;
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
