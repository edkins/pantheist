package io.pantheist.handler.sql.backend;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.FilterableObjectStream;
import io.pantheist.handler.sql.model.SqlProperty;

public interface SqlService
{
	void startOrRestart();

	void stop();

	void deleteAllTables();

	void createTable(String tableName, List<SqlProperty> columns);

	void updateOrInsert(String tableName, List<SqlProperty> columns, AntiIterator<ObjectNode> values);

	FilterableObjectStream select(String table, List<SqlProperty> columns);
}
