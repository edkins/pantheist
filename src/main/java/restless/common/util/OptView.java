package restless.common.util;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface OptView<T>
{
	boolean isPresent();

	/**
	 * @throws NoSuchElementException if not present
	 */
	T get();

	<U> OptView<U> map(Function<T, U> fn);

	<U> ListView<U> flatMap(Function<T, ? extends ListView<U>> fn);

	<U> OptView<U> optMap(Function<T, ? extends OptView<U>> fn);

	OptView<T> filter(Predicate<T> predicate);

	ImmutableOpt<T> immutableCopy();

	T orElse(Supplier<T> supplier);

	/**
	 * Returns true if it exists and has the given value.
	 */
	boolean hasValue(T expectedValue);
}
