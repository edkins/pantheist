package restless.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

final class SingletonImpl<T> implements ImmutableOpt<T>, Possible<T>
{
	private final T value;

	private SingletonImpl(@Nullable final T value)
	{
		this.value = value;
	}

	static <T> SingletonImpl<T> of(@Nullable final T value)
	{
		return new SingletonImpl<>(value);
	}

	@Override
	public <U> SingletonImpl<U> map(final Function<T, U> fn)
	{
		return of(fn.apply(value));
	}

	@Override
	public <U> OptView<U> optMap(final Function<T, ? extends OptView<U>> fn)
	{
		return fn.apply(value);
	}

	@Override
	public OptView<T> filter(final Predicate<T> predicate)
	{
		if (predicate.test(value))
		{
			return this;
		}
		else
		{
			return View.empty();
		}
	}

	@Override
	public boolean isPresent()
	{
		return true;
	}

	@Override
	public T get()
	{
		return value;
	}

	@Override
	public ImmutableOpt<T> immutableCopy()
	{
		return this;
	}

	@Override
	public T orElse(final Supplier<T> supplier)
	{
		return value;
	}

	@Override
	public <U> ListView<U> flatMap(final Function<T, ? extends ListView<U>> fn)
	{
		return fn.apply(value);
	}

	@Override
	public FailureReason failure()
	{
		throw new IllegalStateException("Not in failed state");
	}

	@Override
	public <U> Possible<U> posMap(final Function<T, Possible<U>> fn)
	{
		return fn.apply(value);
	}

	@Override
	public Possible<T> onSuccess(final Runnable run)
	{
		run.run();
		return this;
	}

	@Override
	public boolean hasValue(final T expectedValue)
	{
		checkNotNull(expectedValue);
		return value.equals(expectedValue);
	}

	@Override
	public <U> Possible<U> coerce()
	{
		throw new IllegalStateException("Not in failed state");
	}

}
