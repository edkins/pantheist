package restless.api.management.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import restless.api.management.backend.ManagementBackend;
import restless.api.management.model.ConfigRequest;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.model.Schema;

/**
 * Path segments may be:
 *
 *   +literal refers to an exact path segment
 *   *        matches any single path segment
 *   **       matches zero or more path segments
 *   config   management function
 *   data     management function
 *   schema   management function
 */
@Path("/")
public final class ManagementResourceImpl implements ManagementResource
{
	private static final Logger LOGGER = LogManager.getLogger(ManagementResourceImpl.class);
	private final ManagementBackend backend;
	private final ObjectMapper objectMapper;

	@Inject
	ManagementResourceImpl(final ManagementBackend backend, final ObjectMapper objectMapper)
	{
		this.backend = checkNotNull(backend);
		this.objectMapper = checkNotNull(objectMapper);
	}

	/**
	 * Right now just used for the test checking that the server is alive
	 */
	@GET
	public Response root()
	{
		return Response.ok("need a home page I suppose").build();
	}

	/**
	 * Handles the config management function
	 */
	@PUT
	@Path("{path:([+*][^/]*[/])*}config")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putConfig(@PathParam("path") final String path, final String configJson) throws IOException
	{
		LOGGER.info("PUT {}config", path);

		try
		{
			final ConfigRequest request = objectMapper.readValue(configJson, ConfigRequest.class);

			backend.putConfig(backend.pathSpec(path), request);

			return Response.noContent().build();
		}
		catch (final RuntimeException ex)
		{
			return errorResponse(ex);
		}
	}

	/**
	 * Handles the data management function (PUT)
	 */
	@PUT
	@Path("{path:([+*][^/]*[/])*}data")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response putData(@PathParam("path") final String path, final String data)
	{
		LOGGER.info("PUT {}data", path);

		try
		{
			backend.putData(backend.pathSpec(path), data);

			return Response.noContent().build();
		}
		catch (final RuntimeException ex)
		{
			return errorResponse(ex);
		}
	}

	/**
	 * Handles the data management function (GET)
	 */
	@GET
	@Path("{path:([+*][^/]*[/])*}data")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getData(@PathParam("path") final String path)
	{
		LOGGER.info("GET {}data", path);

		try
		{
			final PossibleData data = backend.getData(backend.pathSpec(path));

			return possibleDataResponse(data);
		}
		catch (final RuntimeException ex)
		{
			return errorResponse(ex);
		}
	}

	/**
	 * Handles the schema management function (PUT)
	 */
	@PUT
	@Path("{path:([+*][^/]*[/])*}schema")
	@Consumes("application/schema+json")
	public Response putSchema(@PathParam("path") final String path, final String data)
	{
		LOGGER.info("PUT {}schema", path);

		try
		{
			final JsonNode jsonNode = objectMapper.readValue(data, JsonNode.class);
			backend.putJsonSchema(backend.pathSpec(path), jsonNode);

			return Response.noContent().build();
		}
		catch (final RuntimeException ex)
		{
			return errorResponse(ex);
		}
		catch (final IOException e)
		{
			return jsonValidationResponse(e);
		}
	}

	/**
	 * Handles the schema management function (GET)
	 */
	@GET
	@Path("{path:([+*][^/]*[/])*}schema")
	public Response getSchema(@PathParam("path") final String path)
	{
		LOGGER.info("GET {}schema", path);

		try
		{
			final Schema schema = backend.getSchema(backend.pathSpec(path));
			return Response.ok(schema.contentAsString(), schema.httpContentType()).build();
		}
		catch (final RuntimeException ex)
		{
			return errorResponse(ex);
		}
	}

	private Response jsonValidationResponse(final IOException e)
	{
		LOGGER.catching(e);
		return Response.status(400).entity("Bad json").build();
	}

	private Response errorResponse(final RuntimeException ex)
	{
		LOGGER.catching(ex);
		throw ex;
	}

	private Response possibleDataResponse(final PossibleData data)
	{
		if (data.isPresent())
		{
			return Response.ok(data.get()).build();
		}
		else
		{
			LOGGER.info("Returning status " + data.httpStatus());
			return Response.status(data.httpStatus()).build();
		}
	}
}
