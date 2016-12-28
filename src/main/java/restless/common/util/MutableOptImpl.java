package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class MutableOptImpl<T> implements MutableOpt<T>
{
	// State
	private ImmutableOpt<T> value;

	private MutableOptImpl(final ImmutableOpt<T> value)
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
		this.value = View.single(newValue);
	}

	@Override
	public void supplyOpt(final OptView<T> other)
	{
		checkNotNull(other);
		if (other.isPresent())
		{
			supply(other.get());
		}
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
		value = View.empty();
	}

	public static <T> MutableOpt<T> empty()
	{
		return new MutableOptImpl<>(View.empty());
	}

	@Override
	public <U> OptView<U> map(final Function<T, U> fn)
	{
		return value.map(fn);
	}

	@Override
	public <U> OptView<U> optMap(final Function<T, ? extends OptView<U>> fn)
	{
		return value.optMap(fn);
	}

	@Override
	public OptView<T> filter(final Predicate<T> predicate)
	{
		return value.filter(predicate);
	}

	@Override
	public void setSingle(final T item)
	{
		this.value = View.single(item);
	}

	@Override
	public ImmutableOpt<T> immutableCopy()
	{
		return value;
	}

	@Override
	public T orElse(final Supplier<T> supplier)
	{
		return value.orElse(supplier);
	}

	@Override
	public <U> ListView<U> flatMap(final Function<T, ? extends ListView<U>> fn)
	{
		return value.flatMap(fn);
	}

	@Override
	public boolean hasValue(final T expectedValue)
	{
		return value.hasValue(expectedValue);
	}
}
