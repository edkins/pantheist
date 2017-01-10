package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Supplier;

final class SupplierSupplier<T> implements Dep<Supplier<T>>
{
	private final Supplier<T> dep;

	private SupplierSupplier(final Supplier<T> dep)
	{
		this.dep = checkNotNull(dep);
	}

	static <T> Dep<Supplier<T>> of(final Supplier<T> dep)
	{
		return new SupplierSupplier<>(dep);
	}

	@Override
	public Supplier<T> get()
	{
		return dep;
	}

	@Override
	public EventType signal(final boolean major)
	{
		return EventType.MINOR;
	}

	@Override
	public String toString()
	{
		return "Supplier " + dep;
	}
}
