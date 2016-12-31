package io.pantheist.testclient.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.client.Client;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.common.util.OtherPreconditions;

final class TargetRootImpl implements TargetRoot
{
	private final Client client;
	private final String uriPrefix;
	private final ObjectMapper objectMapper;

	TargetRootImpl(final Client client, final String uriPrefix, final ObjectMapper objectMapper)
	{
		this.client = checkNotNull(client);
		this.uriPrefix = OtherPreconditions.checkNotNullOrEmpty(uriPrefix);
		this.objectMapper = checkNotNull(objectMapper);
	}

	@Override
	public TargetWrapper home()
	{
		return new TargetWrapper(this, client.target(uriPrefix), objectMapper);
	}

	@Override
	public TargetWrapper forUri(final String uri)
	{
		if (uri.startsWith(uriPrefix))
		{
			return new TargetWrapper(this, client.target(uri), objectMapper);
		}
		else
		{
			throw new IllegalArgumentException("Cannot target uri outside of our space: " + uri);
		}
	}

}
