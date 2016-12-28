package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

final class MakeListImpl<T, R> implements MakeList<T, R>
{
	private final Function<SubIterator<T>, R> func;

	private MakeListImpl(final Function<SubIterator<T>, R> func)
	{
		this.func = checkNotNull(func);
	}

	static <T, R> MakeList<T, R> fromFunc(final Function<SubIterator<T>, R> func)
	{
		return new MakeListImpl<>(func);
	}

	private <U> MakeList<U, R> chain(final Function<SubIterator<U>, SubIterator<T>> fn)
	{
		return fromFunc(it -> func.apply(fn.apply(it)));
	}

	@Override
	public MakeList<T, R> withFirst(final T firstItem)
	{
		checkNotNull(firstItem);
		return chain(xs -> Sub.concat(Sub.single(firstItem), xs));
	}

	@Override
	public MakeList<T, R> withLast(final T lastItem)
	{
		checkNotNull(lastItem);
		return chain(xs -> Sub.concat(xs, Sub.single(lastItem)));
	}

	@Override
	public MakeList<T, R> without(final Predicate<T> dropPredicate)
	{
		checkNotNull(dropPredicate);
		return chain(xs -> Sub.filter(xs, dropPredicate.negate()));
	}

	@Override
	public R from(final List<T> list)
	{
		checkNotNull(list);
		return func.apply(Sub.from(list));
	}

	@Override
	public R from(final List<T> xs, final T x)
	{
		return withLast(x).from(xs);
	}

	@Override
	public MakeList<T, R> tail()
	{
		return chain(s -> {
			if (!s.tryAdvance(x -> {
			}))
			{
				throw new IllegalStateException("tail: empty list");
			}
			return s;
		});
	}

	@Override
	public MakeList<T, R> init()
	{
		return chain(xs -> new SubIterator<T>() {
			private boolean start = true;
			private T current = null;

			private final Consumer<T> get = x -> {
				current = x;
			};

			@Override
			public boolean tryAdvance(final Consumer<? super T> action)
			{
				if (start)
				{
					if (!xs.tryAdvance(get))
					{
						throw new IllegalStateException("init: empty list");
					}
					start = false;
				}
				final T previous = current;
				if (xs.tryAdvance(get))
				{
					action.accept(previous);
					return true;
				}
				else
				{
					return false;
				}
			}
		});
	}

}
