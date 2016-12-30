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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import restless.api.management.backend.ManagementBackend;
import restless.api.management.model.CreateConfigRequest;
import restless.api.management.model.ListClassifierResponse;
import restless.api.management.model.ListConfigResponse;
import restless.api.management.model.ListJavaPkgResponse;
import restless.common.http.Resp;
import restless.common.util.Escapers;
import restless.common.util.FailureReason;
import restless.common.util.Possible;

@Path("/")
public final class ManagementResourceImpl implements ManagementResource
{
	private static final Logger LOGGER = LogManager.getLogger(ManagementResourceImpl.class);
	private final ManagementBackend backend;
	private final ObjectMapper objectMapper;
	private final Resp resp;

	@Inject
	ManagementResourceImpl(final ManagementBackend backend,
			final ObjectMapper objectMapper,
			final Resp resp)
	{
		this.backend = checkNotNull(backend);
		this.objectMapper = checkNotNull(objectMapper);
		this.resp = checkNotNull(resp);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response listRoot()
	{
		LOGGER.info("GET /");
		try
		{
			return resp.toJson(backend.listRootClassifiers());
		}
		catch (final RuntimeException e)
		{
			return resp.unexpectedError(e);
		}
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
				return resp.failure(data.failure());
			}
		}
		catch (final RuntimeException | JsonProcessingException e)
		{
			return resp.unexpectedError(e);
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

			return resp.possibleEmpty(result);
		}
		catch (final JsonProcessingException e)
		{
			return resp.jsonValidation(e);
		}
		catch (final RuntimeException | IOException e)
		{
			return resp.unexpectedError(e);
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
				return resp.failure(FailureReason.DOES_NOT_EXIST);
			}
		}
		catch (final RuntimeException e)
		{
			return resp.unexpectedError(e);
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

			return resp.possibleEmpty(result);
		}
		catch (final RuntimeException e)
		{
			return resp.unexpectedError(e);
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

			return resp.possibleEmpty(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
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

			return resp.possibleData(data);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
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

	/**
	 * Handles listing classifiers within a java package (GET)
	 */
	@GET
	@Path("java-pkg/{pkg}")
	@Produces("application/json")
	public Response listJavaPkgClassifiers(@PathParam("pkg") final String pkg)
	{
		LOGGER.info("GET java-pkg/{}", pkg);
		try
		{
			final Possible<ListClassifierResponse> result = backend.listJavaPackageClassifiers(pkg);
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles listing java packages (GET)
	 */
	@GET
	@Path("java-pkg")
	@Produces("application/json")
	public Response listJavaPkg()
	{
		LOGGER.info("GET java-pkg");
		try
		{
			final ListJavaPkgResponse result = backend.listJavaPackages();
			return resp.toJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles the java management function (PUT)
	 */
	@PUT
	@Path("java-pkg/{pkg}/file/{file}/data")
	@Consumes("text/plain")
	public Response putJerseyFile(
			@PathParam("pkg") final String pkg,
			@PathParam("file") final String file,
			final String data)
	{
		LOGGER.info("PUT java-pkg/{}/file/{}/data", pkg, file);
		try
		{
			final Possible<Void> result = backend.putJavaFile(pkg, file, data);
			return resp.possibleEmpty(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles the java management function (GET)
	 */
	@GET
	@Path("java-pkg/{pkg}/file/{file}/data")
	@Produces("text/plain")
	public Response getJerseyFile(
			@PathParam("pkg") final String pkg,
			@PathParam("file") final String file)
	{
		LOGGER.info("GET java-pkg/{}/file/{}/data", pkg, file);
		try
		{
			final Possible<String> result = backend.getJavaFile(pkg, file);
			return resp.possibleData(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
