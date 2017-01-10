package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

final class SingletonProvider<T> implements Dep<T>
{
	private final T object;

	private SingletonProvider(final T object)
	{
		this.object = checkNotNull(object);
	}

	static <T> Dep<T> of(final T object)
	{
		return new SingletonProvider<>(object);
	}

	@Override
	public T get()
	{
		return object;
	}

	@Override
	public EventType signal(final boolean major)
	{
		throw new UnsupportedOperationException("SingletonProvider has no dependencies so should not be signaled");
	}

	@Override
	public String toString()
	{
		return "Single " + object;
	}
}
