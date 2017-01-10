package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;
import java.util.function.Supplier;

final class ConstructorAdaptor<T, U extends Notifiable> implements Dep<U>
{
	private final Function<T, U> fn;
	private final Supplier<T> dep;

	// State
	private U value;

	private ConstructorAdaptor(final Function<T, U> fn, final Supplier<T> dep)
	{
		this.fn = checkNotNull(fn);
		this.dep = checkNotNull(dep);
		this.value = fn.apply(dep.get());
	}

	static <T, U extends Notifiable> Dep<U> from(
			final Function<T, U> fn,
			final Supplier<T> dep)
	{
		return new ConstructorAdaptor<>(fn, dep);
	}

	@Override
	public U get()
	{
		return value;
	}

	@Override
	public EventType signal(final boolean major)
	{
		if (major)
		{
			value = fn.apply(dep.get());
			return EventType.MAJOR;
		}
		else
		{
			final boolean propagate = value.signal();
			return propagate ? EventType.MINOR : EventType.NONE;
		}
	}
}
