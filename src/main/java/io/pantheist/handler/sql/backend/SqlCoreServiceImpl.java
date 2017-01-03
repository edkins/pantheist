package io.pantheist.handler.sql.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.system.config.PantheistConfig;

final class SqlCoreServiceImpl implements SqlCoreService
{
	private final Pattern WORD = Pattern.compile("[a-z][a-z_]*");

	private final Set<String> reservedWordsLowercase;

	private final PantheistConfig config;

	private final ObjectMapper objectMapper;

	@Inject
	private SqlCoreServiceImpl(final PantheistConfig config, final ObjectMapper objectMapper)
	{
		this.reservedWordsLowercase = new HashSet<>();
		this.config = checkNotNull(config);
		this.objectMapper = checkNotNull(objectMapper);
	}

	@Override
	public void startOrRestart() throws UnsupportedEncodingException, IOException
	{
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				SqlServiceImpl.class.getResourceAsStream("/postgres-reserved-words.txt"), "utf-8")))
		{
			reservedWordsLowercase.clear();
			while (reader.ready())
			{
				final String line = reader.readLine();
				if (line != null && !line.isEmpty())
				{
					reservedWordsLowercase.add(line.trim().toLowerCase());
				}
			}
		}
	}

	@Override
	public String toSqlTableName(final String tableName)
	{
		OtherPreconditions.checkNotNullOrEmpty(tableName);
		final String sqlName = tableName.replace('-', '_');
		if (!validSqlTableName(sqlName))
		{
			throw new IllegalArgumentException("Invalid table name: " + tableName);
		}
		return sqlName;
	}

	@Override
	public Connection connect() throws SQLException
	{
		final String path = String.format("jdbc:postgresql://localhost:%d/postgres", config.postgresPort());
		return DriverManager.getConnection(path, "giles", null);
	}

	@Override
	public boolean validSqlTableName(final String tableName)
	{
		return WORD.matcher(tableName).matches() && !reservedWordsLowercase.contains(tableName.toLowerCase());
	}

	@Override
	public String toSqlColumnName(final String name)
	{
		final String sqlName = name.toLowerCase();
		if (!validSqlColumnName(sqlName))
		{
			throw new IllegalArgumentException("Bad column name: " + name);
		}
		return sqlName;
	}

	@Override
	public void setStatementValue(
			final Connection connection,
			final PreparedStatement statement,
			final int parameterIndex,
			final JsonNode v)
			throws SQLException, JsonProcessingException
	{
		if (v.isBoolean())
		{
			statement.setBoolean(parameterIndex, v.booleanValue());
		}
		else if (v.isTextual())
		{
			statement.setString(parameterIndex, v.textValue());
		}
		else if (v.isArray() || v.isObject())
		{
			final PGobject jsonObject = new PGobject();
			jsonObject.setType("json");
			jsonObject.setValue(objectMapper.writeValueAsString(v));
			statement.setObject(parameterIndex, jsonObject);
		}
		else
		{
			throw new UnsupportedOperationException("Unrecognized property type: " + v.getNodeType());
		}

	}

	private boolean validSqlColumnName(final String columnName)
	{
		return WORD.matcher(columnName).matches() && !reservedWordsLowercase.contains(columnName.toLowerCase());
	}

}
