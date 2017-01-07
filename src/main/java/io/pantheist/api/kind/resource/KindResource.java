package io.pantheist.api.kind.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.api.kind.backend.KindBackend;
import io.pantheist.common.annotations.ResourceTag;
import io.pantheist.common.http.Resp;
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
	 * Handles creating a new kind (POST)
	 */
	@POST
	@Path("entity/kind/kind/create")
	@Consumes("application/json")
	public Response postKind(final String data)
	{
		LOGGER.info("PUT kind/kind/create");
		try
		{
			return resp.possibleLocation(
					resp.request(data, Kind.class).posMap(backend::postKind));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles creating a new instance of a particular kind (POST)
	 *
	 * This does not take a data payload.
	 */
	@POST
	@Path("entity/kind/{kindId}/new")
	public Response newInstanceOfKind(@PathParam("kindId") final String kindId)
	{
		LOGGER.info("PUT kind/{}/new", kindId);
		try
		{
			return resp.possibleLocation(backend.newInstanceOfKind(kindId));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
