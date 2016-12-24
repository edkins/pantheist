package restless.client.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import restless.client.api.ManagementClientException;
import restless.client.api.ManagementResourceNotFoundException;
import restless.client.api.ManagementUnexpectedResponseException;
import restless.client.api.ManagementUnsupportedException;
import restless.client.api.ResponseType;
import restless.common.util.DummyException;
import restless.common.util.OtherPreconditions;

public final class TargetWrapper
{
	private final WebTarget target;
	private final ObjectMapper objectMapper;

	TargetWrapper(final WebTarget target, final ObjectMapper objectMapper)
	{
		this.target = checkNotNull(target);
		this.objectMapper = checkNotNull(objectMapper);
	}

	public TargetWrapper withSegment(final String segment)
	{
		OtherPreconditions.checkNotNullOrEmpty(segment);
		return new TargetWrapper(target.path(segment), objectMapper);
	}

	public String getTextPlain()
	{
		return getString("text/plain");
	}

	public ResponseType getTextPlainResponseType()
	{
		final Response response = target.request(MediaType.TEXT_PLAIN).get();
		return responseType(response);
	}

	public void putTextPlain(final String text)
	{
		final Response response = target.request().put(Entity.text(text));
		expectNoContent(response, "PUT");
	}

	public void putObjectAsJson(final Object obj)
	{
		try
		{
			final String json = objectMapper.writeValueAsString(obj);
			final Response response = target.request().put(Entity.json(json));
			expectNoContent(response, "PUT");
		}
		catch (final JsonProcessingException e)
		{
			throw new ManagementClientException("ClientException " + target.getUri().toString(), e);
		}
	}

	private void expectNoContent(final Response response, final String hint)
	{
		if (response.getStatus() == 204 && !response.hasEntity())
		{
			response.readEntity(String.class);
		}
		else
		{
			errorResponse(response, hint);
		}
	}

	private String expectContent(final Response response, final String hint)
	{
		if (response.getStatus() == 200 && response.hasEntity())
		{
			return response.readEntity(String.class);
		}
		else
		{
			throw errorResponse(response, hint);
		}
	}

	/**
	 * @return never
	 * @throws ManagementResourceNotFoundException
	 *             on 404
	 * @throws ManagementUnsupportedException
	 *             on 405 or 501
	 * @throws ManagementUnexpectedResponseException
	 *             on anything else
	 */
	private DummyException errorResponse(final Response response, final String hint)
	{
		final String message = response.getStatus() + " " + hint + " " + target.getUri().toString();
		switch (response.getStatus()) {
		case 404:
			throw new ManagementResourceNotFoundException(message);
		case 405: // method not allowed
		case 501: // not implemented
			throw new ManagementUnsupportedException(message);
		default:
			throw unexpectedResponse(message, response);
		}
	}

	private DummyException unexpectedResponse(final String message, final Response response)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(message).append(' ');
		if (response.hasEntity())
		{
			sb.append(response.readEntity(String.class));
		}
		else
		{
			sb.append("[no entity]");
		}
		throw new ManagementUnexpectedResponseException(sb.toString());
	}

	private ResponseType responseType(final Response response)
	{
		switch (response.getStatus()) {
		case 200:
			return ResponseType.OK;
		case 204:
			return ResponseType.NO_CONTENT;
		case 404:
			return ResponseType.NOT_FOUND;
		case 501: // not implemented
			return ResponseType.NOT_IMPLEMENTED;
		case 400: // bad request
		case 405: // method not allowed
		default:
			return ResponseType.UNEXPECTED;
		}
	}

	private void putStream(final InputStream input, final String contentType)
	{
		final Response response = target.request().put(Entity.entity(input, contentType));
		expectNoContent(response, "PUT");
	}

	public void putResource(final String resourcePath, final String contentType)
	{
		try (InputStream input = TargetWrapper.class.getResourceAsStream(resourcePath))
		{
			putStream(input, contentType);
		}
		catch (final IOException e)
		{
			throw new ManagementClientException("Error reading resource file", e);
		}
	}

	public String getString(final String contentType)
	{
		final Response response = target.request(contentType).get();
		return expectContent(response, "GET");
	}
}
