package restless.api.something.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public final class SomethingResourceImpl implements SomethingResource
{
	@GET
	public Response root()
	{
		return Response.ok("need a home page I suppose").build();
	}

	@GET
	@Path("foo")
	public Response foo()
	{
		return Response.ok("hello").build();
	}
}
