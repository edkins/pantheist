package restless.common.util;

import java.util.function.Consumer;

/**
 * Exposes a subset of the Spliterator operations.
 */
@Deprecated
public interface SubIterator<T>
{
	boolean tryAdvance(Consumer<? super T> action);
}
