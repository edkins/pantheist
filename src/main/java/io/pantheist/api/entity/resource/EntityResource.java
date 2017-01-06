package io.pantheist.api.entity.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private final KindBackend backend;
	private final Resp resp;

	@Inject
	private EntityResource(final KindBackend backend, final Resp resp)
	{
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
			final Possible<ListEntityResponse> result = backend.listEntitiesWithKind(kindId);
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
			final Possible<KindedMime> result = backend.getEntity(kindId, entityId);
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
			final ListClassifierResponse result = backend.listEntityClassifiers();
			return resp.toJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
