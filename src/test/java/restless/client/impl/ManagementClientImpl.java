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
	private final URI managementUri;
	private final URI mainUri;
	private final ObjectMapper objectMapper;

	private ManagementClientImpl(final Client client, final URI managementUri, final URI mainUri,
			final ObjectMapper objectMapper)
	{
		this.client = checkNotNull(client);
		this.managementUri = checkNotNull(managementUri);
		this.mainUri = checkNotNull(mainUri);
		this.objectMapper = checkNotNull(objectMapper);
	}

	public static ManagementClient from(final URL managementUrl, final URL mainUrl, final ObjectMapper objectMapper)
	{
		try
		{
			return new ManagementClientImpl(ClientBuilder.newClient(), managementUrl.toURI(), mainUrl.toURI(),
					objectMapper);
		}
		catch (final URISyntaxException e)
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public ManagementPath manage()
	{
		return new ManagementPathImpl(new TargetWrapper(client.target(managementUri), objectMapper));
	}

	@Override
	public TargetWrapper main()
	{
		return new TargetWrapper(client.target(mainUri), objectMapper);
	}
}
