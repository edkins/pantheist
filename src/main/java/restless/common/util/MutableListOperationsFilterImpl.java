package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;
import java.util.function.Predicate;

final class MutableListOperationsFilterImpl<T> implements MutableListOperations<T>
{
	private final MutableListOperations<T> base;
	private final Predicate<T> predicate;

	private MutableListOperationsFilterImpl(final MutableListOperations<T> base, final Predicate<T> predicate)
	{
		this.base = checkNotNull(base);
		this.predicate = checkNotNull(predicate);
	}

	static <T> MutableListOperations<T> of(final MutableListOperations<T> base, final Predicate<T> predicate)
	{
		return new MutableListOperationsFilterImpl<>(base, predicate);
	}

	@Override
	public ListView<T> list()
	{
		return base.list().filter(predicate);
	}

	@Override
	public void removeIf(final Predicate<T> filter)
	{
		base.removeIf(x -> predicate.test(x) && filter.test(x));
	}

	@Override
	public void transform(final Function<T, T> fn)
	{
		base.transform(x -> {
			if (predicate.test(x))
			{
				final T y = fn.apply(x);
				if (!predicate.test(y))
				{
					throw new IllegalArgumentException("Inserted value would be rejected by predicate: " + y);
				}
				return y;
			}
			else
			{
				return x;
			}
		});
	}

	@Override
	public void insertAtEnd(final T item)
	{
		if (!predicate.test(item))
		{
			throw new IllegalArgumentException("Inserted value would be rejected by predicate: " + item);
		}
		base.insertAtEnd(item);
	}

}
