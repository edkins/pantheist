package restless.common.util;

import java.util.function.Function;
import java.util.function.Predicate;

public interface MutableListView<T>
{
	<U> MutableListView<U> translate(Function<T, U> getter, Function<U, T> setter);

	MutableListOperations<T> basic();

	<K> MutableByKey<K, T> organizeByKey(Function<T, K> keyGetter);

	void clear();

	void setSingle(T item);

	MutableListView<T> filter(final Predicate<T> predicate);
}
