package io.pantheist.api.kind.resource;

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

import io.pantheist.api.kind.backend.KindBackend;
import io.pantheist.api.kind.model.ApiKind;
import io.pantheist.api.kind.model.ListEntityResponse;
import io.pantheist.common.annotations.ResourceTag;
import io.pantheist.common.http.Resp;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.kind.model.Kind;

@Path("/")
public class KindResource implements ResourceTag
{
	private static final Logger LOGGER = LogManager.getLogger(KindResource.class);
	private final KindBackend backend;
	private final Resp resp;

	@Inject
	private KindResource(
			final KindBackend backend,
			final ObjectMapper objectMapper,
			final Resp resp)
	{
		this.backend = checkNotNull(backend);
		this.resp = checkNotNull(resp);
	}

	/**
	 * Handles listing kinds (GET)
	 */
	@GET
	@Path("kind")
	@Produces("application/json")
	public Response listKinds()
	{
		LOGGER.info("GET kind");
		try
		{
			return resp.toJson(backend.listKinds());
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles kind data (PUT)
	 */
	@PUT
	@Path("kind/{kindId}/data")
	@Consumes("application/json")
	public Response putKind(
			@PathParam("kindId") final String kindId,
			final String data)
	{
		LOGGER.info("PUT kind/{}/data", kindId);
		try
		{
			return resp.possibleEmpty(
					resp.request(data, Kind.class)
							.posMap(kind -> backend.putKindData(kindId, kind)));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles kind data (GET)
	 */
	@GET
	@Path("kind/{kindId}/data")
	@Produces("application/json")
	public Response getKindData(
			@PathParam("kindId") final String kindId)
	{
		LOGGER.info("GET kind/{}/data", kindId);
		try
		{
			final Possible<Kind> result = backend.getKindData(kindId);
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles kind information (GET)
	 */
	@GET
	@Path("kind/{kindId}")
	@Produces("application/json")
	public Response getKindInfo(
			@PathParam("kindId") final String kindId)
	{
		LOGGER.info("GET kind/{}", kindId);
		try
		{
			final Possible<ApiKind> result = backend.getKindInfo(kindId);
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
