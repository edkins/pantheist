package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

final class MakeSingleImpl<T, R> implements MakeSingle<T, R>
{
	private final Function<T, R> func;

	private MakeSingleImpl(final Function<T, R> func)
	{
		this.func = checkNotNull(func);
	}

	static <T, R> MakeSingleImpl<T, R> fromFunc(final Function<T, R> func)
	{
		return new MakeSingleImpl<>(func);
	}

	@Override
	public R of(final T x)
	{
		return func.apply(x);
	}

	@Override
	public <U> MakeList<U, R> fromList(
			final Supplier<T> empty,
			final Function<U, T> single,
			final Function<List<U>, T> multiple)
	{
		return through(list -> {
			if (list.isEmpty())
			{
				return empty.get();
			}
			else if (list.size() == 1)
			{
				return single.apply(list.get(0));
			}
			else
			{
				return multiple.apply(list);
			}
		});
	}

	@Override
	public <U> MakeList<U, R> through(final Function<List<U>, T> listFunc)
	{
		return throughAntiIterator(ait -> listFunc.apply(ait.toList()));
	}

	@Override
	public <U> MakeList<U, R> snowball(final T initial, final BiFunction<T, U, T> accumulate)
	{
		return throughAntiIterator(ait -> ait.snowball(initial, accumulate));
	}

	@Override
	public <U> MakeList<U, R> throughAntiIterator(final Function<AntiIterator<U>, T> aitFunc)
	{
		return MakeListImpl.fromAntiIt(ait -> func.apply(aitFunc.apply(ait)));
	}

}
