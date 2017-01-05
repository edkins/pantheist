package io.pantheist.common.api.model;

import static com.google.common.base.Preconditions.checkNotNull;

import io.pantheist.common.util.OtherPreconditions;

public final class KindedImpl<T> implements Kinded<T>
{
	private final String kindUrl;
	private final T data;

	private KindedImpl(final String kindUrl, final T data)
	{
		this.kindUrl = OtherPreconditions.checkNotNullOrEmpty(kindUrl);
		this.data = checkNotNull(data);
	}

	public static <T> Kinded<T> of(final String kindUrl, final T data)
	{
		return new KindedImpl<>(kindUrl, data);
	}

	@Override
	public String kindUrl()
	{
		return kindUrl;
	}

	@Override
	public T data()
	{
		return data;
	}

}
