package io.pantheist.api.entity.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.pantheist.api.entity.backend.EntityBackend;
import io.pantheist.api.entity.model.AddRequest;
import io.pantheist.api.kind.backend.KindBackend;
import io.pantheist.api.kind.model.ListEntityResponse;
import io.pantheist.common.annotations.ResourceTag;
import io.pantheist.common.api.model.KindedMime;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.http.Resp;
import io.pantheist.common.util.Possible;

@Path("/")
public final class EntityResource implements ResourceTag
{
	private static final Logger LOGGER = LogManager.getLogger(EntityResource.class);
	private final KindBackend kindBackend;
	private final Resp resp;
	private final EntityBackend backend;

	@Inject
	private EntityResource(final KindBackend kindBackend, final EntityBackend backend, final Resp resp)
	{
		this.kindBackend = checkNotNull(kindBackend);
		this.backend = checkNotNull(backend);
		this.resp = checkNotNull(resp);
	}

	/**
	 * Handles listing entities by kind (GET)
	 */
	@GET
	@Path("entity/{kindId}")
	@Produces("application/json")
	public Response listEntitiesWithKind(
			@PathParam("kindId") final String kindId)
	{
		LOGGER.info("GET entity/{}", kindId);
		try
		{
			final Possible<ListEntityResponse> result = kindBackend.listEntitiesWithKind(kindId);
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles fetching a particular entity (GET)
	 *
	 * The mime type is unknown at compile time because it will be different for different kinds.
	 */
	@GET
	@Path("entity/{kindId}/{entityId}")
	public Response getEntity(
			@PathParam("kindId") final String kindId,
			@PathParam("entityId") final String entityId)
	{
		LOGGER.info("GET entity/{}/{}", kindId, entityId);
		try
		{
			final Possible<KindedMime> result = kindBackend.getEntity(kindId, entityId);
			return resp.possibleKindedMime(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Lists all possible entity kinds (GET)
	 */
	@GET
	@Path("entity")
	@Produces("application/json")
	public Response listEntityClassifiers()
	{
		LOGGER.info("GET entity");
		try
		{
			final ListClassifierResponse result = kindBackend.listEntityClassifiers();
			return resp.toJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles "add" operation (POST)
	 *
	 * Note this currently does not return a url. It's assumed that the thing that's added
	 * is not encapsulated as a resource with a url, it's just a component of an existing resource.
	 */
	@POST
	@Path("entity/{kindId}/{entityId}/add")
	@Consumes("application/json")
	public Response add(
			@PathParam("kindId") final String kindId,
			@PathParam("entityId") final String entityId,
			final String requestJson)
	{
		LOGGER.info("POST entity/{}/{}/add", kindId, entityId);
		try
		{
			final Possible<Void> result = resp.request(requestJson, AddRequest.class)
					.posMap(req -> backend.add(kindId, entityId, req));
			return resp.possibleEmpty(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
