package restless.api.kind.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import restless.api.kind.backend.KindBackend;
import restless.api.kind.model.ApiComponent;
import restless.api.kind.model.ApiEntity;
import restless.api.kind.model.ListComponentResponse;
import restless.common.http.Resp;
import restless.common.util.Possible;
import restless.handler.kind.model.Kind;

@Path("/")
public class KindResourceImpl implements KindResource
{
	private static final Logger LOGGER = LogManager.getLogger(KindResourceImpl.class);
	private final KindBackend backend;
	private final ObjectMapper objectMapper;
	private final Resp resp;

	@Inject
	private KindResourceImpl(
			final KindBackend backend,
			final ObjectMapper objectMapper,
			final Resp resp)
	{
		this.backend = checkNotNull(backend);
		this.objectMapper = checkNotNull(objectMapper);
		this.resp = checkNotNull(resp);
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
			return resp.possibleEmpty(result);
		}
		catch (JsonParseException | JsonMappingException e)
		{
			return resp.jsonValidation(e);
		}
		catch (final RuntimeException | IOException ex)
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
			final Possible<ApiEntity> result = backend.getEntity(entityId);
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

	/**
	 * Handles kinds (PUT)
	 */
	@PUT
	@Path("kind/{kindId}")
	@Consumes("application/json")
	public Response putKind(
			@PathParam("kindId") final String kindId,
			final String data)
	{
		LOGGER.info("PUT kind/{}", kindId);
		try
		{
			final Kind kind = objectMapper.readValue(data, Kind.class);
			final Possible<Void> result = backend.putKind(kindId, kind);
			return resp.possibleEmpty(result);
		}
		catch (JsonParseException | JsonMappingException e)
		{
			return resp.jsonValidation(e);
		}
		catch (final RuntimeException | IOException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles kinds (GET)
	 */
	@GET
	@Path("kind/{kindId}")
	@Produces("application/json")
	public Response getKind(
			@PathParam("kindId") final String kindId)
	{
		LOGGER.info("GET kind/{}", kindId);
		try
		{
			final Possible<Kind> result = backend.getKind(kindId);
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
