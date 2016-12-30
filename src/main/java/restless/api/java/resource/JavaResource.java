package restless.api.java.resource;

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

import restless.api.java.backend.JavaBackend;
import restless.api.management.model.ListClassifierResponse;
import restless.api.management.model.ListJavaPkgResponse;
import restless.common.annotations.ResourceTag;
import restless.common.http.Resp;
import restless.common.util.Possible;

@Path("/")
public final class JavaResource implements ResourceTag
{
	private static final Logger LOGGER = LogManager.getLogger(JavaResource.class);
	private final JavaBackend backend;
	private final Resp resp;

	@Inject
	private JavaResource(final JavaBackend backend, final Resp resp)
	{
		this.backend = checkNotNull(backend);
		this.resp = checkNotNull(resp);
	}

	/**
	 * Handles listing classifiers within a java package (GET)
	 */
	@GET
	@Path("java-pkg/{pkg}")
	@Produces("application/json")
	public Response listJavaPkgClassifiers(@PathParam("pkg") final String pkg)
	{
		LOGGER.info("GET java-pkg/{}", pkg);
		try
		{
			final Possible<ListClassifierResponse> result = backend.listJavaPackageClassifiers(pkg);
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles listing java packages (GET)
	 */
	@GET
	@Path("java-pkg")
	@Produces("application/json")
	public Response listJavaPkg()
	{
		LOGGER.info("GET java-pkg");
		try
		{
			final ListJavaPkgResponse result = backend.listJavaPackages();
			return resp.toJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles the java management function (PUT)
	 */
	@PUT
	@Path("java-pkg/{pkg}/file/{file}/data")
	@Consumes("text/plain")
	public Response putJerseyFile(
			@PathParam("pkg") final String pkg,
			@PathParam("file") final String file,
			final String data)
	{
		LOGGER.info("PUT java-pkg/{}/file/{}/data", pkg, file);
		try
		{
			final Possible<Void> result = backend.putJavaFile(pkg, file, data);
			return resp.possibleEmpty(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles the java management function (GET)
	 */
	@GET
	@Path("java-pkg/{pkg}/file/{file}/data")
	@Produces("text/plain")
	public Response getJerseyFile(
			@PathParam("pkg") final String pkg,
			@PathParam("file") final String file)
	{
		LOGGER.info("GET java-pkg/{}/file/{}/data", pkg, file);
		try
		{
			final Possible<String> result = backend.getJavaFile(pkg, file);
			return resp.possibleData(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
