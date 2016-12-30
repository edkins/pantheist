package restless.api.entity.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import restless.api.entity.backend.EntityBackend;
import restless.api.entity.model.ApiComponent;
import restless.api.entity.model.ApiEntity;
import restless.api.entity.model.ListComponentResponse;
import restless.common.annotations.ResourceTag;
import restless.common.http.Resp;
import restless.common.util.Possible;

@Path("/")
public final class EntityResource implements ResourceTag
{
	private static final Logger LOGGER = LogManager.getLogger(EntityResource.class);
	private final EntityBackend backend;
	private final Resp resp;

	@Inject
	private EntityResource(final EntityBackend backend, final Resp resp)
	{
		this.backend = checkNotNull(backend);
		this.resp = checkNotNull(resp);
	}

	/**
	 * Handles listing entities (GET)
	 */
	@GET
	@Path("entity")
	@Produces("application/json")
	public Response listEntities()
	{
		LOGGER.info("GET entity");
		try
		{
			return resp.toJson(backend.listEntities());
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
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
			final Possible<Void> result = resp.request(request, ApiEntity.class)
					.posMap(entity -> backend.putApiEntity(entityId, entity));
			return resp.possibleEmpty(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
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
			final Possible<ApiEntity> result = backend.getApiEntity(entityId);
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
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
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
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
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

}
