package io.pantheist.api.schema.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.pantheist.api.schema.backend.SchemaBackend;
import io.pantheist.common.annotations.ResourceTag;
import io.pantheist.common.http.Resp;
import io.pantheist.common.util.Possible;

@Path("/")
public final class SchemaResource implements ResourceTag
{
	private static final Logger LOGGER = LogManager.getLogger(SchemaResource.class);
	private final SchemaBackend backend;
	private final Resp resp;

	@Inject
	private SchemaResource(final SchemaBackend backend, final Resp resp)
	{
		this.backend = checkNotNull(backend);
		this.resp = checkNotNull(resp);
	}

	/**
	 * Handles the schema data validation function (POST)
	 */
	@POST
	@Produces("application/schema+json")
	@Path("entity/json-schema/{schemaId}/validate")
	public Response validateAgainstSchema(
			@PathParam("schemaId") final String schemaId,
			final String data)
	{
		LOGGER.info("GET json-schema/{}", schemaId);
		try
		{
			final Possible<Void> result = backend.validateAgainstJsonSchema(schemaId, data);
			return resp.possibleEmpty(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles creating a new json schema based on id (POST)
	 */
	@POST
	@Path("kind/json-schema/create")
	@Consumes("application/schema+json")
	public Response postJsonSchema(final String data)
	{
		LOGGER.info("POST kind/json-schema/create");
		try
		{
			final Possible<String> result = backend.postJsonSchema(data);

			return resp.possibleLocation(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

}
