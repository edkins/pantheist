package io.pantheist.api.flatdir.resource;

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

import io.pantheist.api.flatdir.backend.FlatDirBackend;
import io.pantheist.common.annotations.ResourceTag;
import io.pantheist.common.http.Resp;

@Path("/")
public class FlatDirResource implements ResourceTag
{
	private static final Logger LOGGER = LogManager.getLogger(FlatDirResource.class);
	private final FlatDirBackend backend;
	private final Resp resp;

	@Inject
	private FlatDirResource(final FlatDirBackend backend, final Resp resp)
	{
		this.backend = checkNotNull(backend);
		this.resp = checkNotNull(resp);
	}

	/**
	 * Lists all the directories (GET)
	 */
	@GET
	@Path("flat-dir")
	@Produces("application/json")
	public Response listFlatDirs()
	{
		LOGGER.info("GET flat-dir");
		try
		{
			return resp.toJson(backend.listFlatDirs());
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Lists all the files in a directory (GET)
	 */
	@GET
	@Path("flat-dir/{dir}/file")
	@Produces("application/json")
	public Response listFlatDirFiles(@PathParam("dir") final String dir)
	{
		LOGGER.info("GET flat-dir/{}/file", dir);
		try
		{
			return resp.possibleToJson(backend.listFiles(dir));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Lists classifiers within a directory (GET)
	 */
	@GET
	@Path("flat-dir/{dir}")
	@Produces("application/json")
	public Response listFlatDirClassifiers(@PathParam("dir") final String dir)
	{
		LOGGER.info("GET flat-dir/{}", dir);
		try
		{
			return resp.possibleToJson(backend.listFlatDirClassifiers(dir));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Get file information (GET)
	 */
	@GET
	@Path("flat-dir/{dir}/file/{file}")
	@Produces("application/json")
	public Response getFileInfo(@PathParam("dir") final String dir, @PathParam("file") final String file)
	{
		LOGGER.info("GET flat-dir/{}/file/{file}", dir, file);
		try
		{
			return resp.possibleToJson(backend.getFileInfo(dir, file));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Get file data (GET)
	 */
	@GET
	@Path("flat-dir/{dir}/file/{file}/data")
	@Produces("text/plain")
	public Response getFileData(@PathParam("dir") final String dir, @PathParam("file") final String file)
	{
		LOGGER.info("GET flat-dir/{}/file/{file}/data", dir, file);
		try
		{
			return resp.possibleData(backend.getFileData(dir, file));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Put file data (PUT)
	 */
	@PUT
	@Path("flat-dir/{dir}/file/{file}/data")
	@Consumes("text/plain")
	public Response getFileData(@PathParam("dir") final String dir, @PathParam("file") final String file,
			final String data)
	{
		LOGGER.info("GET flat-dir/{}/file/{file}/data", dir, file);
		try
		{
			return resp.possibleEmpty(backend.putFileDataString(dir, file, data, false));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
