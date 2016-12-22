package restless.testhelpers.actions.api;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import restless.testhelpers.actions.interf.RestlessActions;

public class RestlessActionsApi implements RestlessActions
{
	private final Client client;
	private final URI baseUri;
	private final ObjectMapper objectMapper;

	private RestlessActionsApi(final Client client, final URI baseUri, final ObjectMapper objectMapper)
	{
		this.client = checkNotNull(client);
		this.baseUri = checkNotNull(baseUri);
		this.objectMapper = checkNotNull(objectMapper);
	}

	public static RestlessActions from(final URL baseUrl, final ObjectMapper objectMapper)
	{
		try
		{
			return new RestlessActionsApi(ClientBuilder.newClient(), baseUrl.toURI(), objectMapper);
		}
		catch (final URISyntaxException e)
		{
			throw Throwables.propagate(e);
		}
	}

	private String expectOk(final Response response)
	{
		assertEquals(200, response.getStatus());
		assertTrue("Has entity", response.hasEntity());
		return response.readEntity(String.class);
	}

	@Override
	public String foo()
	{
		return expectOk(client.target(baseUri).path("foo").request().get());
	}
}
