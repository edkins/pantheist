package restless.common.util;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Offers convenience methods for working with lists
 */
public interface ListView<T>
{
	Stream<T> stream();

	<U> ListView<U> map(Function<T, U> fn);

	<U> ListView<U> optMap(Function<T, ? extends OptView<U>> fn);

	<U> ListView<U> flatMap(Function<T, ? extends ListView<U>> fn);

	ListView<T> filter(Predicate<T> predicate);

	List<T> toList();

	OptView<T> failIfMultiple();

	void forEach(Consumer<T> consumer);

	//<K> ByKey<K, T> organizeByKey(Function<T, K> keyGetter);

	@SuppressWarnings("unchecked")
	boolean hasSequenceOfItems(T... items);
}
