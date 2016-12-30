package restless.api.schema.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import restless.api.schema.backend.SchemaBackend;
import restless.common.http.Resp;
import restless.common.util.Possible;

@Path("/")
public final class SchemaResourceImpl implements SchemaResource
{
	private static final Logger LOGGER = LogManager.getLogger(SchemaResourceImpl.class);
	private final SchemaBackend backend;
	private final Resp resp;

	@Inject
	private SchemaResourceImpl(final SchemaBackend backend, final Resp resp)
	{
		this.backend = checkNotNull(backend);
		this.resp = checkNotNull(resp);
	}

	/**
	 * Handles the schema management function (PUT)
	 */
	@PUT
	@Path("json-schema/{schemaId}")
	@Consumes("application/schema+json")
	public Response putSchema(
			@PathParam("schemaId") final String schemaId,
			final String data)
	{
		LOGGER.info("PUT json-schema/{}", schemaId);
		try
		{
			final Possible<Void> result = backend.putJsonSchema(schemaId, data);

			return resp.possibleEmpty(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles the schema management function (GET)
	 */
	@GET
	@Produces("application/schema+json")
	@Path("json-schema/{schemaId}")
	public Response getSchema(
			@PathParam("schemaId") final String schemaId)
	{
		LOGGER.info("GET json-schema/{}", schemaId);
		try
		{
			final Possible<String> data = backend.getJsonSchema(schemaId);
			return resp.possibleData(data);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles the schema data validation function (POST)
	 */
	@POST
	@Produces("application/schema+json")
	@Path("json-schema/{schemaId}/validate")
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

}
