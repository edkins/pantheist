package io.pantheist.api.sql.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.pantheist.api.sql.backend.SqlBackend;
import io.pantheist.common.annotations.ResourceTag;
import io.pantheist.common.http.Resp;

@Path("/")
public class SqlResource implements ResourceTag
{
	private static final Logger LOGGER = LogManager.getLogger(SqlResource.class);
	private final SqlBackend backend;
	private final Resp resp;

	@Inject
	private SqlResource(final SqlBackend backend, final Resp resp)
	{
		this.backend = checkNotNull(backend);
		this.resp = checkNotNull(resp);
	}

	/**
	 * Handles listing sql tables (GET)
	 */
	@GET
	@Path("sql-table")
	@Produces("application/json")
	public Response listSqlTables()
	{
		LOGGER.info("GET sql-table");
		try
		{
			return resp.toJson(backend.listSqlTables());
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles listing classifiers for an sql table (GET)
	 */
	@GET
	@Path("sql-table/{table}")
	@Produces("application/json")
	public Response listTableClassifiers(@PathParam("table") final String table)
	{
		LOGGER.info("GET sql-table/{}", table);
		try
		{
			return resp.possibleToJson(backend.listSqlTableClassifiers(table));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles listing all rows in a particular table (GET)
	 *
	 * This breaks the usual pattern: the third segment is a variable here, identifying which column
	 * in the table we want to search by.
	 */
	@GET
	@Path("sql-table/{table}/{column}")
	@Produces("application/json")
	public Response listTableClassifiers(
			@PathParam("table") final String table,
			@PathParam("column") final String column)
	{
		LOGGER.info("GET sql-table/{}/{}", table, column);
		try
		{
			return resp.possibleToJson(backend.listRows(table, column));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}

	/**
	 * Handles retrieving info about a particular row (GET)
	 */
	@GET
	@Path("sql-table/{table}/{column}/{row}")
	@Produces("application/json")
	public Response listTableClassifiers(
			@PathParam("table") final String table,
			@PathParam("column") final String column,
			@PathParam("row") final String row)
	{
		LOGGER.info("GET sql-table/{}/{}/{}", table, column, row);
		try
		{
			return resp.possibleToJson(backend.getRowInfo(table, column, row));
		}
		catch (final RuntimeException ex)
		{
			return resp.unexpectedError(ex);
		}
	}
}
