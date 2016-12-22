package restless.client.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import restless.client.api.ManagementClient;
import restless.client.api.ManagementPath;

public class ManagementClientImpl implements ManagementClient
{
	private final Client client;
	private final URI baseUri;
	private final ObjectMapper objectMapper;

	private ManagementClientImpl(final Client client, final URI baseUri, final ObjectMapper objectMapper)
	{
		this.client = checkNotNull(client);
		this.baseUri = checkNotNull(baseUri);
		this.objectMapper = checkNotNull(objectMapper);
	}

	public static ManagementClient from(final URL baseUrl, final ObjectMapper objectMapper)
	{
		try
		{
			return new ManagementClientImpl(ClientBuilder.newClient(), baseUrl.toURI(), objectMapper);
		}
		catch (final URISyntaxException e)
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public ManagementPath manage()
	{
		return new ManagementPathImpl(new TargetWrapper(client.target(baseUri), objectMapper));
	}
}
