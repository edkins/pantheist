package io.pantheist.common.util;

import java.util.function.Function;

import javax.annotation.Nullable;

final class PossibleOkImpl<T> implements Possible<T>
{
	private final T value;

	private PossibleOkImpl(@Nullable final T value)
	{
		this.value = value;
	}

	static <T> PossibleOkImpl<T> of(@Nullable final T value)
	{
		return new PossibleOkImpl<>(value);
	}

	@Override
	public <U> PossibleOkImpl<U> map(final Function<T, U> fn)
	{
		return of(fn.apply(value));
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
	public <U> Possible<U> coerce()
	{
		throw new IllegalStateException("Not in failed state");
	}

}
