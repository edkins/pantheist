package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

final class MutableOptImpl<T> implements MutableOpt<T>
{
	// State
	private Optional<T> value;

	private MutableOptImpl(final Optional<T> value)
	{
		this.value = checkNotNull(value);
	}

	@Override
	public void supply(final T newValue)
	{
		checkNotNull(newValue);
		if (this.value.isPresent())
		{
			throw new IllegalStateException("Multiple values");
		}
		this.value = Optional.of(newValue);
	}

	@Override
	public boolean isPresent()
	{
		return value.isPresent();
	}

	@Override
	public T get()
	{
		if (!value.isPresent())
		{
			throw new IllegalStateException("No values");
		}
		return value.get();
	}

	@Override
	public void clear()
	{
		value = Optional.empty();
	}

	public static <T> MutableOpt<T> empty()
	{
		return new MutableOptImpl<>(Optional.empty());
	}

	@Override
	public void setSingle(final T item)
	{
		this.value = Optional.of(item);
	}

	@Override
	public Optional<T> toOptional()
	{
		return value;
	}
}
