package restless.api.management.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import restless.api.management.backend.ManagementBackend;
import restless.api.management.model.CreateConfigRequest;
import restless.handler.binding.backend.PossibleData;
import restless.handler.binding.backend.PossibleEmpty;
import restless.handler.binding.model.BindingModelFactory;
import restless.handler.binding.model.Handler;
import restless.handler.binding.model.Schema;

/**
 * Path segments may be:
 *
 *   +literal    refers to an exact path segment
 *   *           matches any single path segment
 *   **          matches zero or more path segments
 *   config      management function
 *   data        management function
 *   schema      management function
 *   jersey-file management function
 */
@Path("/")
public final class ManagementResourceImpl implements ManagementResource
{
	private static final Logger LOGGER = LogManager.getLogger(ManagementResourceImpl.class);
	private final ManagementBackend backend;
	private final BindingModelFactory bindingModelFactory;
	private final ObjectMapper objectMapper;

	@Inject
	ManagementResourceImpl(final ManagementBackend backend,
			final BindingModelFactory bindingModelFactory,
			final ObjectMapper objectMapper)
	{
		this.backend = checkNotNull(backend);
		this.bindingModelFactory = checkNotNull(bindingModelFactory);
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
	 * Handles creating new config
	 */
	@POST
	@Path("config")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createConfig(final String requestJson)
	{
		LOGGER.info("POST config {}", requestJson);

		try
		{
			final CreateConfigRequest request = objectMapper.readValue(requestJson, CreateConfigRequest.class);

			final URI newUri = backend.createConfig(request);

			return Response.created(newUri).build();
		}
		catch (final JsonProcessingException e)
		{
			return jsonValidationResponse(e);
		}
		catch (final RuntimeException | IOException e)
		{
			return unexpectedErrorResponse(e);
		}
	}

	/**
	 * Handles setting the handler
	 */
	@PUT
	@Path("config/{configId}/handler")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putConfig(@PathParam("configId") final String configId, final String handlerJson)
	{
		LOGGER.info("PUT config/{}/handler {}", configId, handlerJson);

		try
		{
			final Handler request = objectMapper.readValue(handlerJson, Handler.class);

			final PossibleEmpty result = backend.putConfig(bindingModelFactory.configId(configId), request);

			return possibleEmptyResponse(result);
		}
		catch (final JsonProcessingException e)
		{
			return jsonValidationResponse(e);
		}
		catch (final RuntimeException | IOException e)
		{
			return unexpectedErrorResponse(e);
		}
	}

	/**
	 * Handles the data management function (PUT)
	 */
	@PUT
	@Path("data/{path:.*}")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response putData(@PathParam("path") final String path, final String data)
	{
		LOGGER.info("PUT data/{}", path);

		try
		{
			final PossibleEmpty result = backend.putData(backend.literalPath(path), data);

			return possibleEmptyResponse(result);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles the data management function (GET)
	 */
	@GET
	@Path("data/{path:.*}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getData(@PathParam("path") final String path)
	{
		LOGGER.info("GET data/{}", path);

		try
		{
			final PossibleData data = backend.getData(backend.literalPath(path));

			return possibleDataResponse(data);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles the schema management function (PUT)
	 */
	@PUT
	@Path("config/{configId}/schema")
	@Consumes("application/schema+json")
	public Response putSchema(@PathParam("configId") final String configId, final String data)
	{
		LOGGER.info("PUT config/{configId}/schema (json-schema)", configId);

		try
		{
			final JsonNode jsonNode = objectMapper.readValue(data, JsonNode.class);
			final PossibleEmpty result = backend.putJsonSchema(bindingModelFactory.configId(configId), jsonNode);

			return possibleEmptyResponse(result);
		}
		catch (final JsonProcessingException e)
		{
			return jsonValidationResponse(e);
		}
		catch (final RuntimeException | IOException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles the schema management function (GET)
	 */
	@GET
	@Path("config/{configId}/schema")
	public Response getSchema(@PathParam("configId") final String configId)
	{
		LOGGER.info("GET config/{configId}/schema", configId);

		try
		{
			final Schema schema = backend.getSchema(bindingModelFactory.configId(configId));
			return Response.ok(schema.contentAsString(), schema.httpContentType()).build();
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles the jersey-file management function (PUT)
	 */
	@PUT
	@Path("config/{configId}/jersey-file")
	@Consumes("text/plain")
	public Response putJerseyFile(@PathParam("configId") final String configId, final String data)
	{
		LOGGER.info("PUT config/{configId}/jersey-file", configId);

		try
		{
			final PossibleEmpty result = backend.putJerseyFile(bindingModelFactory.configId(configId), data);
			return possibleEmptyResponse(result);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles the jersey-file management function (GET)
	 */
	@GET
	@Path("config/{configId}/jersey-file")
	@Produces("text/plain")
	public Response getJerseyFile(@PathParam("configId") final String configId)
	{
		LOGGER.info("GET config/{configId}/jersey-file", configId);

		try
		{
			final PossibleData result = backend.getJerseyFile(bindingModelFactory.configId(configId));
			return possibleDataResponse(result);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	private Response jsonValidationResponse(final IOException e)
	{
		LOGGER.catching(e);
		return Response.status(400).entity("Bad json").build();
	}

	private Response unexpectedErrorResponse(final Exception ex)
	{
		LOGGER.catching(ex);
		return Response.serverError().entity("Unexpected error").build();
	}

	private Response possibleDataResponse(final PossibleData data)
	{
		if (data.isPresent())
		{
			return Response.ok(data.get()).build();
		}
		else
		{
			LOGGER.info("Returning status " + data.httpStatus() + " " + data.message());
			return Response.status(data.httpStatus()).entity(data.message()).build();
		}
	}

	private Response possibleEmptyResponse(final PossibleEmpty data)
	{
		if (data.isOk())
		{
			return Response.noContent().build();
		}
		else
		{
			LOGGER.info("Returning status " + data.httpStatus() + " " + data.message());
			return Response.status(data.httpStatus()).entity(data.message()).build();
		}
	}
}
