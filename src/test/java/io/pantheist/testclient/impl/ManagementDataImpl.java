package io.pantheist.testclient.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import io.pantheist.testclient.api.ManagementData;
import io.pantheist.testclient.api.ResponseType;

final class ManagementDataImpl implements ManagementData
{
	private final TargetWrapper target;

	ManagementDataImpl(final TargetWrapper target)
	{
		this.target = checkNotNull(target);
	}

	@Override
	public String getString(final String contentType)
	{
		return target.getString(contentType);
	}

	@Override
	public void putString(final String data)
	{
		target.putTextPlain(data);
	}

	@Override
	public void putResource(final String resourcePath, final String contentType)
	{
		target.putResource(resourcePath, contentType);
	}

	@Override
	public ResponseType putResourceResponseType(final String resourcePath, final String contentType)
	{
		return target.putResourceResponseType(resourcePath, contentType);
	}

	@Override
	public ResponseType getResponseTypeForContentType(final String contentType)
	{
		return target.getResponseType(contentType);
	}
}
