package io.pantheist.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

final class PossibleFailureImpl<T> implements Possible<T>
{
	private final FailureReason reason;

	private PossibleFailureImpl(final FailureReason reason)
	{
		this.reason = checkNotNull(reason);
	}

	static <T> Possible<T> of(final FailureReason reason)
	{
		return new PossibleFailureImpl<>(reason);
	}

	@Override
	public <U> Possible<U> map(final Function<T, U> fn)
	{
		return of(reason);
	}

	@Override
	public boolean isPresent()
	{
		return false;
	}

	@Override
	public T get()
	{
		throw new IllegalStateException("Not present: " + reason);
	}

	@Override
	public FailureReason failure()
	{
		return reason;
	}

	@Override
	public <U> Possible<U> posMap(final Function<T, Possible<U>> fn)
	{
		return of(reason);
	}

	@Override
	public Possible<T> onSuccess(final Runnable run)
	{
		return this;
	}

	@Override
	public <U> Possible<U> coerce()
	{
		return of(reason);
	}

}
