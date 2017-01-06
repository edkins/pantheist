package io.pantheist.common.http;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.common.api.model.Kinded;
import io.pantheist.common.api.model.KindedMime;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;

public final class RespImpl implements Resp
{
	private static final Logger LOGGER = LogManager.getLogger(Resp.class);
	private final ObjectMapper objectMapper;

	@Inject
	private RespImpl(final ObjectMapper objectMapper)
	{
		this.objectMapper = checkNotNull(objectMapper);
	}

	@Override
	public <T> Response possibleToJson(final Possible<T> result)
	{
		if (!result.isPresent())
		{
			return failure(result.failure());
		}
		return toJson(result.get());
	}

	@Override
	public <T> Response toJson(final T result)
	{
		checkNotNull(result);
		try
		{
			final String text = objectMapper.writeValueAsString(result);
			return Response.ok(text).build();
		}
		catch (final JsonProcessingException e)
		{
			return unexpectedError(e);
		}
	}

	@Override
	public Response unexpectedError(final Exception ex)
	{
		LOGGER.catching(ex);
		return Response.serverError().entity("Unexpected error").build();
	}

	@Override
	public Response possibleData(final Possible<String> data)
	{
		if (data.isPresent())
		{
			return Response.ok(data.get()).build();
		}
		else
		{
			return failure(data.failure());
		}
	}

	@Override
	public Response possibleEmpty(final Possible<Void> data)
	{
		if (data.isPresent())
		{
			return Response.noContent().build();
		}
		else
		{
			return failure(data.failure());
		}
	}

	@Override
	public Response failure(final FailureReason fail)
	{
		LOGGER.info("Returning status " + fail.httpStatus() + " " + fail.toString());
		return Response.status(fail.httpStatus()).entity(fail.toString()).build();
	}

	@Override
	public <T> Possible<T> request(final String requestJson, final Class<T> clazz)
	{
		try
		{
			return View.ok(objectMapper.readValue(requestJson, clazz));
		}
		catch (final IOException e)
		{
			LOGGER.catching(e);
			return FailureReason.REQUEST_HAS_INVALID_SYNTAX.happened();
		}
	}

	@Override
	public Response possibleLocation(final Possible<String> result)
	{
		try
		{
			if (result.isPresent())
			{
				return Response.created(new URI(result.get())).build();
			}
			else
			{
				return failure(result.failure());
			}
		}
		catch (final URISyntaxException e)
		{
			return unexpectedError(e);
		}
	}

	@Override
	public Response possibleKindedData(final Possible<Kinded<String>> kindedData)
	{
		if (kindedData.isPresent())
		{
			return Response.ok(kindedData.get().data()).link(kindedData.get().kindUrl(), "type").build();
		}
		else
		{
			return failure(kindedData.failure());
		}
	}

	@Override
	public <T> Response possibleKindedJson(final Possible<Kinded<T>> kindedData)
	{
		if (kindedData.isPresent())
		{
			final String text;
			try
			{
				text = objectMapper.writeValueAsString(kindedData.get().data());
			}
			catch (final JsonProcessingException e)
			{
				return unexpectedError(e);
			}
			return Response.ok(text).link(kindedData.get().kindUrl(), "type").build();
		}
		else
		{
			return failure(kindedData.failure());
		}
	}

	@Override
	public Response possibleKindedMime(final Possible<KindedMime> kindedData)
	{
		if (kindedData.isPresent())
		{
			return Response.ok(kindedData.get().text(), kindedData.get().mimeType())
					.link(kindedData.get().kindUrl(), "type")
					.build();
		}
		else
		{
			return failure(kindedData.failure());
		}
	}
}
