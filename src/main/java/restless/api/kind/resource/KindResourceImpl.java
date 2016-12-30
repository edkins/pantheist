package restless.api.kind.resource;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import restless.api.entity.model.ListEntityResponse;
import restless.api.kind.backend.KindBackend;
import restless.api.kind.model.ApiKind;
import restless.common.annotations.ResourceTag;
import restless.common.http.Resp;
import restless.common.util.Possible;

@Path("/")
public class KindResourceImpl implements ResourceTag
{
	private static final Logger LOGGER = LogManager.getLogger(KindResourceImpl.class);
	private final KindBackend backend;
	private final Resp resp;

	@Inject
	private KindResourceImpl(
			final KindBackend backend,
			final ObjectMapper objectMapper,
			final Resp resp)
	{
		this.backend = checkNotNull(backend);
		this.resp = checkNotNull(resp);
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
			return resp.possibleEmpty(
					resp.request(data, ApiKind.class)
							.posMap(kind -> backend.putKind(kindId, kind)));
		}
		catch (final RuntimeException ex)
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
			final Possible<ApiKind> result = backend.getKind(kindId);
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles listing entities by kind (GET)
	 */
	@GET
	@Path("kind/{kindId}/entity")
	@Produces("application/json")
	public Response listEntitiesWithKind(
			@PathParam("kindId") final String kindId)
	{
		LOGGER.info("GET kind/{}/entity", kindId);
		try
		{
			final Possible<ListEntityResponse> result = backend.listEntitiesWithKind(kindId);
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
