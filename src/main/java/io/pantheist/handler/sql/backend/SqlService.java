package io.pantheist.handler.sql.backend;

import java.sql.ResultSet;
import java.util.List;

import io.pantheist.common.shared.model.GenericProperty;
import io.pantheist.common.shared.model.GenericPropertyValue;
import io.pantheist.common.util.AntiIterator;

public interface SqlService
{
	void startOrRestart();

	void stop();

	List<String> listTableNames();

	List<String> listTableIdentifiers(String tableName);

	void deleteAllTables();

	void createTable(String tableName, List<GenericProperty> columns);

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
	 * The list of values must be nonempty.
	 */
	void updateOrInsert(String tableName, List<GenericPropertyValue> values);
}
