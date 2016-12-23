package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

public class MutableOptional<T>
{
	// State
	private Optional<T> value;

	private MutableOptional(final Optional<T> value)
	{
		this.value = checkNotNull(value);
	}

	public void add(final T newValue)
	{
		checkNotNull(newValue);
		if (this.value.isPresent())
		{
			throw new IllegalStateException("Multiple values");
		}
		this.value = Optional.of(newValue);
	}

	public void add(final Optional<T> newValue)
	{
		if (newValue.isPresent())
		{
			add(newValue.get());
		}
	}

	public void add(final MutableOptional<T> other)
	{
		checkNotNull(other);
		if (other.isPresent())
		{
			add(other.get());
		}
	}

	public boolean isPresent()
	{
		return value.isPresent();
	}

	public T get()
	{
		if (!value.isPresent())
		{
			throw new IllegalStateException("No values");
		}
		return value.get();
	}

	public void clear()
	{
		value = Optional.empty();
	}

	public static <T> MutableOptional<T> empty()
	{
		return new MutableOptional<>(Optional.empty());
	}

	public Optional<T> value()
	{
		return value;
	}
}
