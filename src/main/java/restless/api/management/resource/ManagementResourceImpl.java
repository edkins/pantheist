package restless.api.management.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import restless.api.management.backend.ManagementBackend;
import restless.api.management.model.ApiComponent;
import restless.api.management.model.ApiEntity;
import restless.api.management.model.CreateConfigRequest;
import restless.api.management.model.ListComponentResponse;
import restless.api.management.model.ListConfigResponse;
import restless.common.util.Escapers;
import restless.common.util.FailureReason;
import restless.common.util.Possible;

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
	private final ObjectMapper objectMapper;

	@Inject
	ManagementResourceImpl(final ManagementBackend backend,
			final ObjectMapper objectMapper)
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
	 * Lists all the locations for a particular server.
	 */
	@GET
	@Path("server/{serverId}/location")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listConfigs(@PathParam("serverId") final String serverId)
	{
		LOGGER.info("GET server/{}/location", serverId);
		try
		{
			final Possible<ListConfigResponse> data = backend.listLocations(serverId);
			if (data.isPresent())
			{
				final String responseJson = objectMapper.writeValueAsString(data.get());
				LOGGER.info("Returned {} items.", data.get().childResources().size());

				return Response.ok(responseJson).build();
			}
			else
			{
				return failureResponse(data.failure());
			}
		}
		catch (final RuntimeException | JsonProcessingException e)
		{
			return unexpectedErrorResponse(e);
		}
	}

	/**
	 * Handles binding a location to the filesystem
	 */
	@PUT
	@Path("server/{serverId}/location/{locationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createConfig(
			@PathParam("serverId") final String serverId,
			@PathParam("locationId") final String locationId,
			final String requestJson)
	{
		LOGGER.info("PUT server/{}/location/{} {}", serverId, Escapers.url(locationId), requestJson);
		try
		{
			final CreateConfigRequest request = objectMapper.readValue(requestJson, CreateConfigRequest.class);

			final Possible<Void> result = backend.putConfig(serverId, locationId, request);

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
	 * Returns information about the configuration point
	 */
	@GET
	@Path("server/{serverId}/location/{locationId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfig(
			@PathParam("serverId") final String serverId,
			@PathParam("locationId") final String locationId)
	{
		LOGGER.info("GET server/{}/location/{}", serverId, Escapers.url(locationId));
		try
		{
			if (backend.configExists(serverId, locationId))
			{
				return Response.ok("{}").build();
			}
			else
			{
				return failureResponse(FailureReason.DOES_NOT_EXIST);
			}
		}
		catch (final RuntimeException e)
		{
			return unexpectedErrorResponse(e);
		}
	}

	/**
	 * Deletes a configuration point
	 */
	@DELETE
	@Path("server/{serverId}/location/{locationId}")
	public Response deleteConfig(
			@PathParam("serverId") final String serverId,
			@PathParam("locationId") final String locationId)
	{
		LOGGER.info("DELETE server/{}/location/{}", serverId, Escapers.url(locationId));
		try
		{
			final Possible<Void> result = backend.deleteConfig(serverId, locationId);

			return possibleEmptyResponse(result);
		}
		catch (final RuntimeException e)
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
			final Possible<Void> result = backend.putData(path, data);

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
			final Possible<String> data = backend.getData(path);

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

			return possibleEmptyResponse(result);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
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
			return possibleDataResponse(data);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
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
			return possibleEmptyResponse(result);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles the java management function (PUT)
	 */
	@PUT
	@Path("java-pkg/{pkg}/file/{file}")
	@Consumes("text/plain")
	public Response putJerseyFile(
			@PathParam("pkg") final String pkg,
			@PathParam("file") final String file,
			final String data)
	{
		LOGGER.info("PUT java-pkg/{}/file/{}", pkg, file);
		try
		{
			final Possible<Void> result = backend.putJavaFile(pkg, file, data);
			return possibleEmptyResponse(result);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles the java management function (GET)
	 */
	@GET
	@Path("java-pkg/{pkg}/file/{file}")
	@Produces("text/plain")
	public Response getJerseyFile(
			@PathParam("pkg") final String pkg,
			@PathParam("file") final String file)
	{
		LOGGER.info("GET java-pkg/{}/file/{}", pkg, file);
		try
		{
			final Possible<String> result = backend.getJavaFile(pkg, file);
			return possibleDataResponse(result);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles entities (PUT)
	 */
	@PUT
	@Path("entity/{entityId}")
	@Consumes("application/json")
	public Response putEntity(
			@PathParam("entityId") final String entityId, final String request)
	{
		LOGGER.info("PUT entity/{} {}", entityId, request);
		try
		{
			final ApiEntity entity = objectMapper.readValue(request, ApiEntity.class);
			final Possible<Void> result = backend.putEntity(entityId, entity);
			return possibleEmptyResponse(result);
		}
		catch (JsonParseException | JsonMappingException e)
		{
			return jsonValidationResponse(e);
		}
		catch (final RuntimeException | IOException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles entities (GET)
	 */
	@GET
	@Path("entity/{entityId}")
	@Produces("application/json")
	public Response getEntity(
			@PathParam("entityId") final String entityId)
	{
		LOGGER.info("GET entity/{}", entityId);
		try
		{
			final Possible<ApiEntity> result = backend.getEntity(entityId);
			return possibleToJsonResponse(result);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles entity components (GET)
	 */
	@GET
	@Path("entity/{entityId}/component/{componentId}")
	@Produces("application/json")
	public Response getComponent(
			@PathParam("entityId") final String entityId,
			@PathParam("componentId") final String componentId)
	{
		LOGGER.info("GET entity/{}/component/{}", entityId, componentId);
		try
		{
			final Possible<ApiComponent> result = backend.getComponent(entityId, componentId);
			return possibleToJsonResponse(result);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	/**
	 * Handles listing entity components (GET)
	 */
	@GET
	@Path("entity/{entityId}/component")
	@Produces("application/json")
	public Response listComponents(
			@PathParam("entityId") final String entityId)
	{
		LOGGER.info("GET entity/{}/component", entityId);
		try
		{
			final Possible<ListComponentResponse> result = backend.listComponents(entityId);
			return possibleToJsonResponse(result);
		}
		catch (final RuntimeException ex)
		{
			return unexpectedErrorResponse(ex);
		}
	}

	private <T> Response possibleToJsonResponse(final Possible<T> result)
	{
		if (!result.isPresent())
		{
			return failureResponse(result.failure());
		}
		try
		{
			final String text = objectMapper.writeValueAsString(result.get());
			return Response.ok(text).build();
		}
		catch (final JsonProcessingException e)
		{
			return unexpectedErrorResponse(e);
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

	private Response possibleDataResponse(final Possible<String> data)
	{
		if (data.isPresent())
		{
			return Response.ok(data.get()).build();
		}
		else
		{
			return failureResponse(data.failure());
		}
	}

	private Response possibleEmptyResponse(final Possible<Void> data)
	{
		if (data.isPresent())
		{
			return Response.noContent().build();
		}
		else
		{
			return failureResponse(data.failure());
		}
	}

	private Response failureResponse(final FailureReason fail)
	{
		LOGGER.info("Returning status " + fail.httpStatus() + " " + fail.toString());
		return Response.status(fail.httpStatus()).entity(fail.toString()).build();
	}
}
