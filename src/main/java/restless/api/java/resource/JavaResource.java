package restless.api.java.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import restless.api.java.backend.JavaBackend;
import restless.api.java.model.ApiJavaBinding;
import restless.api.java.model.ApiJavaFile;
import restless.api.java.model.ListJavaFileResponse;
import restless.api.java.model.ListJavaPkgResponse;
import restless.common.annotations.ResourceTag;
import restless.common.api.model.ListClassifierResponse;
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
	 * Handles listing java files within a package (GET)
	 */
	@GET
	@Path("java-pkg/{pkg}/file")
	@Produces("application/json")
	public Response listFilesInJavaPkg(@PathParam("pkg") final String pkg)
	{
		LOGGER.info("GET java-pkg/{}/file", pkg);
		try
		{
			final Possible<ListJavaFileResponse> result = backend.listFilesInPackage(pkg);
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles java code (PUT)
	 */
	@PUT
	@Path("java-pkg/{pkg}/file/{file}/data")
	@Consumes("text/plain")
	public Response putJavaCode(
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
	 * Handles java code (GET)
	 */
	@GET
	@Path("java-pkg/{pkg}/file/{file}/data")
	@Produces("text/plain")
	public Response getJavaCode(
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

	/**
	 * Handles java file info (GET)
	 */
	@GET
	@Path("java-pkg/{pkg}/file/{file}")
	@Produces("application/json")
	public Response describeJavaFile(
			@PathParam("pkg") final String pkg,
			@PathParam("file") final String file)
	{
		LOGGER.info("GET java-pkg/{}/file/{}", pkg, file);
		try
		{
			final Possible<ApiJavaFile> result = backend.describeJavaFile(pkg, file);
			return resp.possibleToJson(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles deleting java files (DELETE)
	 */
	@DELETE
	@Path("java-pkg/{pkg}/file/{file}")
	public Response deleteJavaFile(
			@PathParam("pkg") final String pkg,
			@PathParam("file") final String file)
	{
		LOGGER.info("DELETE java-pkg/{pkg}/file/{file}", pkg, file);
		try
		{
			final Possible<Void> result = backend.deleteJavaFile(pkg, file);
			return resp.possibleEmpty(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles retrieving java binding (GET)
	 */
	@GET
	@Path("java-binding")
	public Response getJavaBinding()
	{
		LOGGER.info("GET java-binding");
		try
		{
			final ApiJavaBinding javaBinding = backend.getJavaBinding();
			return resp.toJson(javaBinding);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles binding java root to a different filesystem directory (PUT)
	 */
	@PUT
	@Path("java-binding")
	public Response putJavaBinding(final String data)
	{
		LOGGER.info("PUT java-binding");
		try
		{
			final Possible<Void> result = resp.request(data, ApiJavaBinding.class).posMap(request -> {
				return backend.putJavaBinding(request);
			});
			return resp.possibleEmpty(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
