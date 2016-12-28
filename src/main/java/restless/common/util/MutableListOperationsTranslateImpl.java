package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;
import java.util.function.Predicate;

final class MutableListOperationsTranslateImpl<T, X> implements MutableListOperations<T>
{
	private final MutableListOperations<X> base;
	private final Function<X, T> getter;
	private final Function<T, X> setter;

	private MutableListOperationsTranslateImpl(final MutableListOperations<X> base,
			final Function<X, T> getter,
			final Function<T, X> setter)
	{
		this.base = checkNotNull(base);
		this.getter = checkNotNull(getter);
		this.setter = checkNotNull(setter);
	}

	static <T, X> MutableListOperations<T> of(final MutableListOperations<X> base,
			final Function<X, T> getter,
			final Function<T, X> setter)
	{
		return new MutableListOperationsTranslateImpl<>(base, getter, setter);
	}

	@Override
	public ListView<T> list()
	{
		return base.list().map(getter);
	}

	@Override
	public void removeIf(final Predicate<T> filter)
	{
		base.removeIf(x -> filter.test(getter.apply(x)));
	}

	@Override
	public void transform(final Function<T, T> fn)
	{
		base.transform(x -> {
			final T t0 = getter.apply(x);
			final T t1 = fn.apply(t0);
			if (t0.equals(t1))
			{
				// Avoid disrupting items which were unchanged by the function
				// because getter and setter might not be exact inverses
				return x;
			}
			else
			{
				return setter.apply(t1);
			}
		});
	}

	@Override
	public void insertAtEnd(final T item)
	{
		base.insertAtEnd(setter.apply(item));
	}

}
