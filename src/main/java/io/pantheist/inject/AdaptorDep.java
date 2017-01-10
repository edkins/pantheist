package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

final class AdaptorDep<T, U> implements Dep<U>
{
	private final Function<T, NotifiableSupplier<U>> constructor;
	private final Dep<T> dep;

	// State
	private NotifiableSupplier<U> supplier;

	private AdaptorDep(final Function<T, NotifiableSupplier<U>> constructor, final Dep<T> dep)
	{
		this.constructor = checkNotNull(constructor);
		this.dep = checkNotNull(dep);
		this.supplier = constructor.apply(dep.get());
	}

	static <T, U> Dep<U> of(final Function<T, NotifiableSupplier<U>> constructor, final Dep<T> dep)
	{
		return new AdaptorDep<>(constructor, dep);
	}

	@Override
	public U get()
	{
		return supplier.get();
	}

	@Override
	public EventType signal(final boolean major)
	{
		if (major)
		{
			this.supplier = constructor.apply(dep.get());
		}
		return EventType.MAJOR;
	}

}
