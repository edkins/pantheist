package io.pantheist.handler.sql.backend;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

interface SqlCoreService
{

	String toSqlTableName(String tableName);

	Connection connect() throws SQLException;

	void startOrRestart() throws UnsupportedEncodingException, IOException;

	boolean validSqlTableName(String sqlTableName);

	String toSqlColumnName(String name);

	void setStatementValue(
			final Connection connection,
			final PreparedStatement statement,
			final int parameterIndex,
			final JsonNode v)
			throws SQLException, JsonProcessingException;
}
