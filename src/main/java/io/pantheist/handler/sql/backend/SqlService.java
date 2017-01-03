package io.pantheist.handler.sql.backend;

import java.sql.ResultSet;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.handler.sql.model.SqlProperty;

public interface SqlService
{
	void startOrRestart();

	void stop();

	List<String> listTableNames();

	void deleteAllTables();

	void createTable(String tableName, List<SqlProperty> columns);

	/**
	 * Perform an SQL select for the given column names, returning all rows in the table.
	 *
	 * This returns the SQL ResultSet directly, which must be processed inline (you can't buffer
	 * up a bunch of ResultSets into a list for example).
	 *
	 * The list of column names must be nonempty.
	 */
	AntiIterator<ResultSet> selectAllRows(String tableName, List<String> columnNames);

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

	AntiIterator<ResultSet> selectIndividualRow(
			String tableName,
			String indexColumn,
			JsonNode indexValue,
			List<String> columnNames);

	ObjectNode rsToJsonNode(ResultSet resultSet, List<SqlProperty> columns);
}
