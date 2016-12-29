package restless.common.http;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import restless.common.util.FailureReason;
import restless.common.util.Possible;

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
	public Response jsonValidation(final IOException e)
	{
		LOGGER.catching(e);
		return Response.status(400).entity("Bad json").build();
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
}
