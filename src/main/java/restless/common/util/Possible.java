package restless.common.util;

import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * This is like Optional, but provides more information about why something is missing.
 */
public interface Possible<T>
{
	boolean isPresent();

	/**
	 * @throws NoSuchElementException if not present
	 */
	T get();

	<U> Possible<U> map(Function<T, U> fn);

	<U> Possible<U> posMap(Function<T, Possible<U>> fn);

	/**
	 * @throws IllegalStateException if present.
	 */
	FailureReason failure();

	Possible<T> onSuccess(Runnable run);

	/**
	 * @throws IllegalStateException if present.
	 */
	<U> Possible<U> coerce();
}
