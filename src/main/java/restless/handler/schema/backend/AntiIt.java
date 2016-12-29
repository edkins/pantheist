package restless.handler.schema.backend;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import restless.common.util.MutableOpt;
import restless.common.util.View;

public final class AntiIt
{
	private AntiIt()
	{
		throw new UnsupportedOperationException();
	}

	public static <T> List<T> toList(final AntiIterator<T> ait)
	{
		final ImmutableList.Builder<T> builder = ImmutableList.builder();

		ait.process(x -> {
			builder.add(x);
			return true;
		});
		return builder.build();
	}

	public static <T> Optional<T> findFirst(final AntiIterator<T> ait, final Predicate<T> predicate)
	{
		final MutableOpt<T> result = View.mutableOpt();

		ait.process(x -> {
			if (predicate.test(x))
			{
				result.supply(x);
				return false;
			}
			else
			{
				return true;
			}
		});
		return result.toOptional();
	}
}
