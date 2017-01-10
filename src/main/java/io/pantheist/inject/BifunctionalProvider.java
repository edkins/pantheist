package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.BiFunction;
import java.util.function.Supplier;

final class BifunctionalProvider<T, U, R> implements Dep<R>
{
	private R value;
	private final BiFunction<T, U, R> fn;
	private final Supplier<T> dep1;
	private final Supplier<U> dep2;

	private BifunctionalProvider(
			final BiFunction<T, U, R> fn,
			final Supplier<T> dep1,
			final Supplier<U> dep2)
	{
		this.fn = checkNotNull(fn);
		this.dep1 = checkNotNull(dep1);
		this.dep2 = checkNotNull(dep2);
		this.value = checkNotNull(fn.apply(dep1.get(), dep2.get()));
	}

	public static <T, U, R> Dep<R> from(
			final BiFunction<T, U, R> fn,
			final Supplier<T> dep1,
			final Supplier<U> dep2)
	{
		return new BifunctionalProvider<>(fn, dep1, dep2);
	}

	@Override
	public R get()
	{
		return value;
	}

	@Override
	public EventType signal(final boolean major)
	{
		if (major)
		{
			value = fn.apply(dep1.get(), dep2.get());
			return EventType.MAJOR;
		}
		else
		{
			return EventType.MINOR;
		}
	}

}
