package restless.common.util;

import java.util.List;

/**
 * Apply a MakeList to some data.
 */
public interface MakeListApply<T, R>
{
	/**
	 * Apply this maker to the given list and return the result.
	 */
	R from(List<T> list);

	/**
	 * Shorthand for withLast(x).apply(xs)
	 */
	R from(List<T> xs, T x);

}
