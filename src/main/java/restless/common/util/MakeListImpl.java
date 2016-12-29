package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.function.Function;

final class MakeListImpl<T, R> implements MakeList<T, R>
{
	private final Function<AntiIterator<T>, R> func;

	private MakeListImpl(final Function<AntiIterator<T>, R> func)
	{
		this.func = checkNotNull(func);
	}

	static <T, R> MakeList<T, R> fromAntiIt(final Function<AntiIterator<T>, R> func)
	{
		return new MakeListImpl<>(func);
	}

	private <U> MakeList<U, R> chain(final Function<AntiIterator<U>, AntiIterator<T>> fn)
	{
		return fromAntiIt(it -> func.apply(fn.apply(it)));
	}

	@Override
	public MakeList<T, R> withFirst(final T firstItem)
	{
		checkNotNull(firstItem);
		return chain(xs -> AntiIt.single(firstItem).andThen(xs));
	}

	@Override
	public MakeList<T, R> withLast(final T lastItem)
	{
		checkNotNull(lastItem);
		return chain(xs -> xs.andThen(AntiIt.single(lastItem)));
	}

	@Override
	public R from(final List<T> list)
	{
		checkNotNull(list);
		return func.apply(AntiIt.from(list));
	}

	@Override
	public R from(final List<T> xs, final T x)
	{
		return withLast(x).from(xs);
	}

	@Override
	public MakeList<T, R> tail()
	{
		return drop(1);
	}

	@Override
	public MakeList<T, R> init()
	{
		return chain(AntiIterator::init);
	}

	@Override
	public MakeList<T, R> drop(final int count)
	{
		return chain(xs -> xs.drop(count, true));
	}

	@Override
	public <U> MakeList<U, R> map(final Function<U, T> fn)
	{
		return chain(xs -> xs.map(fn));
	}

}
