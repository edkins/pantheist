package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;
import java.util.function.Supplier;

final class FunctionalProvider<T, U> implements Dep<U>
{
	private final Function<T, U> fn;
	private final Supplier<T> dep;

	// State
	private U value;

	private FunctionalProvider(final Function<T, U> fn, final Supplier<T> dep)
	{
		this.fn = checkNotNull(fn);
		this.dep = checkNotNull(dep);
		this.value = fn.apply(dep.get());
	}

	static <T, U> Dep<U> from(
			final Function<T, U> fn,
			final Supplier<T> dep)
	{
		return new FunctionalProvider<>(fn, dep);
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
			return EventType.MINOR;
		}
	}
}
