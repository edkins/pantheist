package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;
import java.util.function.Predicate;

final class MutableListViewImpl<T> implements MutableListView<T>
{
	private final MutableListOperations<T> base;

	private MutableListViewImpl(final MutableListOperations<T> base)
	{
		this.base = checkNotNull(base);
	}

	static <T> MutableListView<T> of(final MutableListOperations<T> base)
	{
		return new MutableListViewImpl<>(base);
	}

	@Override
	public void setSingle(final T item)
	{
		base.removeIf(x -> true);
		base.insertAtEnd(item);
	}

	@Override
	public <U> MutableListView<U> translate(final Function<T, U> getter, final Function<U, T> setter)
	{
		return of(MutableListOperationsTranslateImpl.of(base, getter, setter));
	}

	@Override
	public void clear()
	{
		base.removeIf(x -> true);
	}

	@Override
	public MutableListView<T> filter(final Predicate<T> predicate)
	{
		return of(MutableListOperationsFilterImpl.of(base, predicate));
	}

	@Override
	public <K> MutableByKey<K, T> organizeByKey(final Function<T, K> keyGetter)
	{
		return MutableByKeyImpl.of(base, keyGetter);
	}

	@Override
	public MutableListOperations<T> basic()
	{
		return base;
	}

}
