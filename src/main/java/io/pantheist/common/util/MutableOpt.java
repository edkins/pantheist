package io.pantheist.common.util;

import java.util.Optional;

public interface MutableOpt<T>
{
	void supply(T newValue);

	void clear();

	void replace(T item);

	boolean isPresent();

	T get();

	Optional<T> toOptional();
}
