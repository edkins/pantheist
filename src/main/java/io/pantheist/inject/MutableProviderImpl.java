package io.pantheist.inject;

import static com.google.common.base.Preconditions.checkNotNull;

final class MutableProviderImpl<T> implements MutableProvider<T>
{
	private T value;
	private final EventOrigin eventOrigin;

	private MutableProviderImpl(final T value, final EventOrigin eventOrigin)
	{
		this.value = checkNotNull(value);
		this.eventOrigin = checkNotNull(eventOrigin);
	}

	static <T> MutableProvider<T> from(final T initialValue, final EventOrigin eventOrigin)
	{
		return new MutableProviderImpl<>(initialValue, eventOrigin);
	}

	@Override
	public T get()
	{
		return value;
	}

	@Override
	public void set(final T newValue)
	{
		this.value = checkNotNull(newValue);
		eventOrigin.fire();
	}

	@Override
	public EventType signal(boolean major)
	{
		throw new UnsupportedOperationException("MutableProviderImpl has no dependencies so should not be signaled");
	}
}
