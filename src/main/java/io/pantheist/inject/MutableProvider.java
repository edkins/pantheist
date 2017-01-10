package io.pantheist.inject;

public interface MutableProvider<T> extends Dep<T>
{
	void set(T newValue);
}
