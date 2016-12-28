package restless.common.util;

import java.util.Optional;

public interface MutableOpt<T>
{
	void supply(T newValue);

	void clear();

	void setSingle(T item);

	boolean isPresent();

	T get();

	Optional<T> toOptional();
}
