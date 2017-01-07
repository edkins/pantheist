package io.pantheist.testclient.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import io.pantheist.testclient.api.ManagementClient;
import io.pantheist.testclient.api.ManagementPathRoot;
import io.pantheist.testclient.api.ManagementPathServer;
import io.pantheist.testclient.api.ManagementUnexpectedResponseException;

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
	public ManagementPathRoot manage()
	{
		return new ManagementPathImpl(managementTarget());
	}

	private TargetWrapper managementTarget()
	{
		return new TargetRootImpl(client, managementUri.toString(), objectMapper).home();
	}

	@Override
	public TargetWrapper main()
	{
		return new TargetRootImpl(client, mainUri.toString(), objectMapper).home();
	}

	@Override
	public ManagementPathServer manageMainServer()
	{
		return manage().server(mainUri.getPort());
	}

	@Override
	public void regenerateDb()
	{
		final Response response = client.target(mainUri.toString()).path("system/regenerate-db").request().post(null);
		if (response.getStatus() != 204)
		{
			throw new ManagementUnexpectedResponseException(
					"Unexpected status code " + response.getStatus() + " when regenerating db");
		}
	}

	@Override
	public void reload()
	{
		final Response response = client.target(mainUri.toString()).path("system/reload").request().post(null);
		if (response.getStatus() != 204)
		{
			throw new ManagementUnexpectedResponseException(
					"Unexpected status code " + response.getStatus() + " when reloading");
		}
	}
}
