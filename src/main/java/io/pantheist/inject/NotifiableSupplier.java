package io.pantheist.inject;

public interface NotifiableSupplier<T>
{
	T get();

	boolean signal();
}
