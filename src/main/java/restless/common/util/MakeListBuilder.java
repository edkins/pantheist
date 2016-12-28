package restless.common.util;

import java.util.function.Predicate;

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
	 * Removes the items that satisfy the predicate.
	 */
	MakeList<T, R> without(Predicate<T> dropPredicate);

	/**
	 * Drops the first element and fails if the list is empty.
	 */
	MakeList<T, R> tail();

	/**
	 * Drops the last element and fails if the list is empty.
	 */
	MakeList<T, R> init();
}
