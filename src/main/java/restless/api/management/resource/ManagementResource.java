package restless.api.management.resource;

import static com.google.common.base.Preconditions.checkNotNull;

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

import restless.api.management.backend.ManagementBackend;
import restless.api.management.model.CreateConfigRequest;
import restless.api.management.model.ListConfigResponse;
import restless.common.annotations.ResourceTag;
import restless.common.http.Resp;
import restless.common.util.Escapers;
import restless.common.util.FailureReason;
import restless.common.util.Possible;

@Path("/")
public final class ManagementResource implements ResourceTag
{
	private static final Logger LOGGER = LogManager.getLogger(ManagementResource.class);
	private final ManagementBackend backend;
	private final Resp resp;

	@Inject
	ManagementResource(final ManagementBackend backend,
			final Resp resp)
	{
		this.backend = checkNotNull(backend);
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
				LOGGER.info("Returned {} items.", data.get().childResources().size());
				return resp.toJson(data.get());
			}
			else
			{
				return resp.failure(data.failure());
			}
		}
		catch (final RuntimeException e)
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
			return resp.possibleEmpty(
					resp.request(requestJson, CreateConfigRequest.class).posMap(
							request -> backend.putConfig(serverId, locationId, request)));
		}
		catch (final RuntimeException e)
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
	 * Handles the system management function: terminate (POST)
	 */
	@POST
	@Path("system/terminate")
	public Response terminate()
	{
		LOGGER.info("POST system/terminate");

		try
		{
			backend.scheduleTerminate();

			return Response.noContent().build();
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles the system management function: reload configuration (POST)
	 */
	@POST
	@Path("system/reload")
	public Response reloadConfiguration()
	{
		LOGGER.info("POST system/reload");

		try
		{
			backend.reloadConfiguration();

			// This is status 202 ACCEPTED. It means processing of the request has not completed.
			return Response.accepted().build();
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

}
