package restless.common.util;

import java.util.function.Function;

/**
 * The methods of MakeList that build a new MakeList.
 */
public interface MakeListBuilder<T, R>
{
	/**
	 * Appends the given item to the start of the list.
	 */
	MakeList<T, R> withFirst(T firstItem);

	/**
	 * Appends the given item to the end of the list.
	 */
	MakeList<T, R> withLast(T lastItem);

	/**
	 * Drops the first element and fails if the list is empty.
	 *
	 * Equivalent to drop(1)
	 */
	MakeList<T, R> tail();

	/**
	 * Drops the last element and fails if the list is empty.
	 */
	MakeList<T, R> init();

	/**
	 * Drop some number of elements from the start. Fails if the list is shorter than that.
	 */
	MakeList<T, R> drop(int count);

	/**
	 * Applies the given function to each element
	 */
	<U> MakeList<U, R> map(Function<U, T> fn);
}
