package io.pantheist.api.java.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.pantheist.api.java.backend.JavaBackend;
import io.pantheist.api.java.model.ApiJavaBinding;
import io.pantheist.api.java.model.ListJavaFileResponse;
import io.pantheist.api.java.model.ListJavaPkgResponse;
import io.pantheist.common.annotations.ResourceTag;
import io.pantheist.common.api.model.Kinded;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.http.Resp;
import io.pantheist.common.util.Possible;

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
	@Path("java-pkg/{pkg}/file/{file}")
	@Consumes("text/plain")
	public Response putJavaCode(
			@PathParam("pkg") final String pkg,
			@PathParam("file") final String file,
			final String data)
	{
		LOGGER.info("PUT java-pkg/{}/file/{}", pkg, file);
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
	 *
	 * Also returns kind url in the 'type' link header
	 */
	@GET
	@Path("java-pkg/{pkg}/file/{file}")
	@Produces("text/plain")
	public Response getJavaCode(
			@PathParam("pkg") final String pkg,
			@PathParam("file") final String file)
	{
		LOGGER.info("GET java-pkg/{}/file/{}", pkg, file);
		try
		{
			final Possible<Kinded<String>> result = backend.getJavaFile(pkg, file);
			return resp.possibleKindedData(result);
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
	@Consumes("application/json")
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

	/**
	 * Handles creating a new java file, detecting the package and filename automatically (POST)
	 *
	 * Note that this is included in the kind resource path.
	 */
	@POST
	@Path("entity/kind/java-file/create")
	@Consumes("text/plain")
	public Response createJavaFile(final String data)
	{
		LOGGER.info("POST kind/java-file/create");
		try
		{
			final Possible<String> result = backend.postJava(data);
			return resp.possibleLocation(result);
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
