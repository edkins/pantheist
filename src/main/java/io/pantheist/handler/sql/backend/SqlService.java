package io.pantheist.handler.sql.backend;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.handler.sql.model.SqlProperty;

public interface SqlService
{
	void startOrRestart();

	void stop();

	void deleteAllTables();

	void createTable(String tableName, List<SqlProperty> columns);

	/**
	 * Insert the specified stuff into the given table, or update if the primary key value is
	 * already in use.
	 *
	 * The values object must be nonempty and must contain primaryKeyColumn.
	 *
	 * Generally, if one of the columns in the original table is missing here, we'll get a
	 * not-null constraint exception from SQL because the tables are created with NOT NULL on
	 * all columns.
	 */
	void updateOrInsert(String tableName, String primaryKeyColumn, ObjectNode values);

	SelectBuilder select(String table, List<SqlProperty> columns);
}
