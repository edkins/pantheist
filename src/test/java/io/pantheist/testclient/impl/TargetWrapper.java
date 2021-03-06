package io.pantheist.testclient.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.common.util.DummyException;
import io.pantheist.common.util.Escapers;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.testclient.api.ManagementClientException;
import io.pantheist.testclient.api.ManagementResourceNotFoundException;
import io.pantheist.testclient.api.ManagementUnexpectedResponseException;
import io.pantheist.testclient.api.ManagementUnsupportedException;
import io.pantheist.testclient.api.ResponseType;

public final class TargetWrapper
{
	private final TargetRoot targetRoot;
	private final WebTarget target;
	private final ObjectMapper objectMapper;

	TargetWrapper(final TargetRoot targetRoot, final WebTarget target, final ObjectMapper objectMapper)
	{
		this.targetRoot = checkNotNull(targetRoot);
		this.target = checkNotNull(target);
		this.objectMapper = checkNotNull(objectMapper);
	}

	public TargetWrapper withSegment(final String segment)
	{
		OtherPreconditions.checkNotNullOrEmpty(segment);
		return new TargetWrapper(targetRoot, target.path(segment), objectMapper);
	}

	public TargetWrapper withEscapedSegment(final String segment)
	{
		OtherPreconditions.checkNotNullOrEmpty(segment);
		return new TargetWrapper(targetRoot, target.path(Escapers.url(segment)), objectMapper);
	}

	public String getTextPlain()
	{
		return getString("text/plain");
	}

	public ResponseType getResponseType(final String contentType)
	{
		final Response response = target.request(contentType).get();
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

	public ResponseType putObjectJsonResponseType(final Object obj)
	{
		try
		{
			final String json = objectMapper.writeValueAsString(obj);
			final Response response = target.request().put(Entity.json(json));
			return responseType(response);
		}
		catch (final JsonProcessingException e)
		{
			throw new ManagementClientException("ClientException " + target.getUri().toString(), e);
		}
	}

	public TargetWrapper createObjectAsJsonWithPostRequest(final Object obj)
	{
		try
		{
			final String json = objectMapper.writeValueAsString(obj);
			final Response response = target.request().post(Entity.json(json));
			final String location = expectCreated(response, "POST");
			return targetRoot.forUri(location);
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

	private String expectCreated(final Response response, final String hint)
	{
		if (response.getStatus() == 201 && response.getHeaders().containsKey("Location"))
		{
			return (String) response.getHeaders().getFirst("Location");
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
		case 201:
			return ResponseType.CREATED;
		case 204:
			return ResponseType.NO_CONTENT;
		case 404:
			return ResponseType.NOT_FOUND;
		case 501: // not implemented
			return ResponseType.NOT_IMPLEMENTED;
		case 400: // bad request
			return ResponseType.BAD_REQUEST;
		case 409: // conflict
			return ResponseType.CONFLICT;
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

	public void putString(final String text, final String contentType)
	{
		final Response response = target.request().put(Entity.entity(text, contentType));
		expectNoContent(response, "PUT");
	}

	public void putResource(final String resourcePath, final String contentType)
	{
		try (InputStream input = TargetWrapper.class.getResourceAsStream(resourcePath))
		{
			if (input == null)
			{
				throw new IllegalArgumentException("Resource not found: " + resourcePath);
			}
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

	public ResponseType putResourceResponseType(final String resourcePath, final String contentType)
	{
		try (InputStream input = TargetWrapper.class.getResourceAsStream(resourcePath))
		{
			final Response response = target.request().put(Entity.entity(input, contentType));
			return responseType(response);
		}
		catch (final IOException e)
		{
			throw new ManagementClientException("Error reading resource file", e);
		}
	}

	public TargetWrapper withSlashSeparatedSegments(final String path)
	{
		TargetWrapper result = this;
		if (path.startsWith("/") || path.endsWith("/"))
		{
			throw new IllegalArgumentException("Relative path must not start or end with slash");
		}
		if (!path.isEmpty())
		{
			for (final String seg : path.split("\\/"))
			{
				result = result.withSegment(seg);
			}
		}
		return result;
	}

	public void delete()
	{
		final Response response = target.request().delete();
		expectNoContent(response, "DELETE");
	}

	public <T> T getJson(final Class<T> clazz)
	{
		final String json = getString("application/json");
		try
		{
			return objectMapper.readValue(json, clazz);
		}
		catch (final IOException e)
		{
			throw new ManagementUnexpectedResponseException(e);
		}
	}

	public String url()
	{
		return target.getUri().toString();
	}

	public boolean exists(final String contentType)
	{
		final ResponseType responseType = getResponseType(contentType);
		switch (responseType) {
		case OK:
			return true;
		case NOT_FOUND:
			return false;
		default:
			throw new ManagementUnexpectedResponseException(responseType);
		}
	}

	public ResponseType postResponseType(final String data, final String contentType)
	{
		final Response response = target.request().post(Entity.entity(data, contentType));
		return responseType(response);
	}

	public ResponseType deleteResponseType()
	{
		final Response response = target.request().delete();
		return responseType(response);
	}

	public TargetWrapper postAndGetPath(final String data, final String contentType)
	{
		final Response response = target.request().post(Entity.entity(data, contentType));
		final String url = expectCreated(response, "POST");
		return targetRoot.forUri(url);
	}

	public String headLink(final String string)
	{
		final Response response = target.request().head();
		if (response.getStatus() == 200 && !response.hasEntity())
		{
			final Link link = response.getLink("type");
			if (link == null)
			{
				throw new ManagementUnexpectedResponseException("No 'type' link returned");
			}
			return link.getUri().toString();
		}
		else
		{
			throw errorResponse(response, "HEAD");
		}
	}

	public <T> void postOperationWithJson(final T value)
	{
		final String text;
		try
		{
			text = objectMapper.writeValueAsString(value);
		}
		catch (final JsonProcessingException e)
		{
			throw new ManagementUnexpectedResponseException(e);
		}

		final Response response = target.request().post(Entity.json(text));
		expectNoContent(response, "POST");
	}

	public <T> ResponseType postOperationWithJsonResponseType(final T value)
	{
		final String text;
		try
		{
			text = objectMapper.writeValueAsString(value);
		}
		catch (final JsonProcessingException e)
		{
			throw new ManagementUnexpectedResponseException(e);
		}

		final Response response = target.request().post(Entity.json(text));
		return responseType(response);
	}
}
