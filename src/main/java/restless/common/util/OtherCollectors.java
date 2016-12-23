package restless.common.util;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

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
	 * Returns an Optional&lt;String&gt; which will be empty if the stream was
	 * empty.
	 */
	public static Collector<String, MutableOptional<StringBuilder>, Optional<String>> join(final String delim)
	{
		return new Collector<String, MutableOptional<StringBuilder>, Optional<String>>() {

			@Override
			public BiConsumer<MutableOptional<StringBuilder>, String> accumulator()
			{
				return (sb, item) -> {
					if (sb.isPresent())
					{
						sb.get().append(delim);
					}
					else
					{
						sb.add(new StringBuilder());
					}
					sb.get().append(item);
				};
			}

			@Override
			public Set<java.util.stream.Collector.Characteristics> characteristics()
			{
				return ImmutableSet.of();
			}

			@Override
			public BinaryOperator<MutableOptional<StringBuilder>> combiner()
			{
				return (sb, sb2) -> {
					if (sb2.isPresent())
					{
						if (sb.isPresent())
						{
							sb.get().append(delim).append(sb2);
						}
						else
						{
							return sb2;
						}
					}
					return sb;
				};
			}

			@Override
			public Function<MutableOptional<StringBuilder>, Optional<String>> finisher()
			{
				return sb -> sb.value().map(StringBuilder::toString);
			}

			@Override
			public Supplier<MutableOptional<StringBuilder>> supplier()
			{
				return () -> MutableOptional.empty();
			}
		};

	}
}
