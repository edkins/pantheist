package restless.api.management.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the following management functions:
 *
 * .config: PUT json
 *
 * .data: PUT text, GET text
 *
 * The paths match any sequence of +foo/+bar/+baz
 *
 * i.e. each segment starts with a "+" and there can be any number of them.
 *
 * They are written out here as a horrible regex.
 */
@Path("/")
public final class ManagementResourceImpl implements ManagementResource
{
	private static final Logger LOGGER = LogManager.getLogger(ManagementResourceImpl.class);

	/**
	 * Right now just used for the test checking that the server is alive
	 */
	@GET
	public Response root()
	{
		return Response.ok("need a home page I suppose").build();
	}

	/**
	 * Handles the .config management function
	 */
	@PUT
	@Path("{path:(\\+[^/]+\\/)*}.config")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putConfig(@PathParam("path") final String path, final String configJson)
	{
		LOGGER.info("PUT {}.config", path);
		return Response.noContent().build();
	}

	/**
	 * Handles the .data management function (PUT)
	 */
	@PUT
	@Path("{path:(\\+[^/]+\\/)*}.data")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response putData(@PathParam("path") final String path, final String data)
	{
		LOGGER.info("PUT {}.data", path);
		return Response.noContent().build();
	}

	/**
	 * Handles the .data management function (GET)
	 */
	@GET
	@Path("{path:(\\+[^/]+\\/)*}.data")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getData(@PathParam("path") final String path)
	{
		LOGGER.info("GET {}.data", path);
		return Response.ok("wrong answer").build();
	}
}
