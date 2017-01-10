package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Supplier;

final class AbsorbEqual<T> implements Dep<T>
{
	private final Supplier<T> dep;
	private T value;

	private AbsorbEqual(final Supplier<T> dep)
	{
		this.dep = checkNotNull(dep);
		this.value = dep.get();
	}

	static <T> Dep<T> of(final Supplier<T> dep)
	{
		return new AbsorbEqual<>(dep);
	}

	@Override
	public EventType signal(boolean major)
	{
		final T newValue = dep.get();
		checkNotNull(newValue);
		final boolean change = !newValue.equals(value);
		value = newValue;
		return change ? EventType.MAJOR : EventType.NONE;
	}

	@Override
	public T get()
	{
		return value;
	}

}
