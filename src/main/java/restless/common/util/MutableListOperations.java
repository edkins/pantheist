package restless.common.util;

import java.util.function.Function;
import java.util.function.Predicate;

public interface MutableListOperations<T>
{
	ListView<T> list();

	void removeIf(Predicate<T> filter);

	void transform(Function<T, T> fn);

	void insertAtEnd(T item);
}
