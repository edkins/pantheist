package io.pantheist.handler.sql.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PGobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

import io.pantheist.api.sql.backend.SqlBackendException;
import io.pantheist.common.shared.model.CommonSharedModelFactory;
import io.pantheist.common.shared.model.GenericPropertyValue;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.handler.sql.model.SqlProperty;
import io.pantheist.system.config.PantheistConfig;

final class SqlServiceImpl implements SqlService
{
	private static final Logger LOGGER = LogManager.getLogger(SqlServiceImpl.class);
	private final FilesystemStore filesystem;

	private final PantheistConfig config;

	private final Pattern WORD = Pattern.compile("[a-z][a-z_]*");

	private final Set<String> reservedWordsLowercase;
	private final ObjectMapper objectMapper;
	private final CommonSharedModelFactory sharedFactory;

	@Inject
	private SqlServiceImpl(
			final FilesystemStore filesystem,
			final PantheistConfig config,
			final ObjectMapper objectMapper,
			final CommonSharedModelFactory sharedFactory)
	{
		this.filesystem = checkNotNull(filesystem);
		this.config = checkNotNull(config);
		this.reservedWordsLowercase = new HashSet<>();
		this.objectMapper = checkNotNull(objectMapper);
		this.sharedFactory = checkNotNull(sharedFactory);
	}

	@Override
	public void startOrRestart()
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

			final FilesystemSnapshot snapshot = filesystem.snapshot();
			final boolean dirExists = snapshot.isDir(dbPath());
			snapshot.isFile(logfilePath());

			snapshot.write(map -> {
				final File dbPath = map.get(dbPath());
				final File logfile = map.get(logfilePath());
				if (!dirExists)
				{
					LOGGER.info("Creating postgres directory {}", dbPath.getAbsolutePath());
					initdb(dbPath);
				}
				LOGGER.info("Starting or restarting postgres");
				restart(dbPath, logfile, config.postgresPort());
			});

			Class.forName("org.postgresql.Driver");
		}
		catch (final ClassNotFoundException | IOException e)
		{
			throw new SqlServiceException(e);
		}
	}

	private boolean validSqlTableName(final String tableName)
	{
		return WORD.matcher(tableName).matches() && !reservedWordsLowercase.contains(tableName.toLowerCase());
	}

	private boolean validSqlColumnName(final String columnName)
	{
		return WORD.matcher(columnName).matches() && !reservedWordsLowercase.contains(columnName.toLowerCase());
	}

	@Override
	public void stop()
	{
		LOGGER.info("Stopping postgres");
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		snapshot.isDir(dbPath());
		snapshot.writeSingle(dbPath(), this::stop);
	}

	@Override
	public void deleteAllTables()
	{
		final List<String> sqlTableNames = listRawTableNames().toList();
		try (final Connection db = connect())
		{
			for (final String sqlTableName : sqlTableNames)
			{
				if (!validSqlTableName(sqlTableName))
				{
					throw new IllegalStateException("Weird table name discovered: " + sqlTableName);
				}
				final String sql = String.format("DROP TABLE %s", sqlTableName);
				try (PreparedStatement statement = db.prepareStatement(sql))
				{
					statement.execute();
				}
				catch (final SQLException e)
				{
					LOGGER.catching(e);
					// couldn't delete? oh well, carry on.
				}
			}
		}
		catch (final SQLException e)
		{
			throw new SqlServiceException(e);
		}
	}

	private String toSqlCreateTableArg(final SqlProperty property)
	{
		final String sqlName = toSqlColumnName(property.name());

		if (property.isPrimaryKey())
		{
			return String.format("%s %s PRIMARY KEY NOT NULL", sqlName, property.toSqlType());
		}
		else
		{
			return String.format("%s %s NOT NULL", sqlName, property.toSqlType());
		}
	}

	private String toSqlColumnName(final String name)
	{
		final String sqlName = name.toLowerCase();
		if (!validSqlColumnName(sqlName))
		{
			throw new IllegalArgumentException("Bad column name: " + name);
		}
		return sqlName;
	}

	@Override
	public void createTable(final String tableName, final List<SqlProperty> columns)
	{
		if (columns.stream().filter(SqlProperty::isPrimaryKey).collect(Collectors.counting()) != 1)
		{
			throw new IllegalArgumentException("Need exactly one primary key");
		}

		final String columnSql = AntiIt.from(columns).map(this::toSqlCreateTableArg).join(",").orElse("");
		final String sql = String.format("CREATE TABLE %s (%s)", toSqlTableName(tableName), columnSql);

		try (final Connection db = connect())
		{
			db.prepareStatement(sql).execute();
		}
		catch (final SQLException e)
		{
			throw new SqlServiceException(e);
		}
	}

	@Override
	public AntiIterator<ResultSet> selectIndividualRow(final String tableName, final GenericPropertyValue indexValue,
			final List<String> columnNames)
	{
		if (columnNames.isEmpty())
		{
			throw new IllegalArgumentException("List of column names must be nonempty");
		}
		final String columnSql = AntiIt.from(columnNames).map(this::toSqlColumnName).join(",").get();
		final String sql = String.format("SELECT %s FROM %s WHERE %s=?",
				columnSql,
				toSqlTableName(tableName),
				toSqlColumnName(indexValue.name()));

		return consumer -> {
			try (final Connection db = connect())
			{
				try (PreparedStatement statement = db.prepareStatement(sql))
				{
					setStatementValue(db, statement, 1, indexValue);
					try (ResultSet resultSet = statement.executeQuery())
					{
						while (resultSet.next())
						{
							consumer.accept(resultSet);
						}
					}
				}
			}
			catch (final SQLException | JsonProcessingException e)
			{
				throw new SqlServiceException(e);
			}
		};
	}

	@Override
	public AntiIterator<ResultSet> selectAllRows(final String tableName, final List<String> columnNames)
	{
		if (columnNames.isEmpty())
		{
			throw new IllegalArgumentException("List of column names must be nonempty");
		}
		final String columnSql = AntiIt.from(columnNames).map(this::toSqlColumnName).join(",").get();
		final String sql = String.format("SELECT %s FROM %s", columnSql, toSqlTableName(tableName));

		return consumer -> {
			try (final Connection db = connect())
			{
				try (PreparedStatement statement = db.prepareStatement(sql))
				{
					try (ResultSet resultSet = statement.executeQuery())
					{
						while (resultSet.next())
						{
							consumer.accept(resultSet);
						}
					}
				}
			}
			catch (final SQLException e)
			{
				throw new SqlServiceException(e);
			}
		};
	}

	@Override
	public void updateOrInsert(
			final String tableName,
			final String primaryKeyColumn,
			final List<GenericPropertyValue> values)
	{
		if (values.isEmpty())
		{
			throw new IllegalArgumentException("List of values must be nonempty");
		}
		final String columnSql = AntiIt.from(values).map(v -> toSqlColumnName(v.name())).join(",").get();
		final String questionMarks = AntiIt.from(values).map(v -> "?").join(",").get();

		final String doUpdateSql = AntiIt.from(values)
				.filter(v -> !v.name().equals(primaryKeyColumn))
				.map(v -> toSqlColumnName(v.name()) + " = EXCLUDED." + toSqlColumnName(v.name()))
				.join(",")
				.get();

		final String sql = String.format("INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (%s) DO UPDATE SET %s",
				toSqlTableName(tableName),
				columnSql,
				questionMarks,
				toSqlColumnName(primaryKeyColumn),
				doUpdateSql);
		try (final Connection db = connect())
		{
			try (PreparedStatement statement = db.prepareStatement(sql))
			{
				setUpStatement(db, statement, values);
				statement.execute();
			}
		}
		catch (final SQLException | JsonProcessingException e)
		{
			throw new SqlServiceException(e);
		}
	}

	private void setUpStatement(
			final Connection connection,
			final PreparedStatement statement,
			final List<GenericPropertyValue> values)
			throws SQLException, JsonProcessingException
	{
		for (int i = 0; i < values.size(); i++)
		{
			final GenericPropertyValue v = values.get(i);
			setStatementValue(connection, statement, i + 1, v);
		}
	}

	private void setStatementValue(
			final Connection connection,
			final PreparedStatement statement, final int parameterIndex,
			final GenericPropertyValue v)
			throws SQLException, JsonProcessingException
	{
		switch (v.typeInfo().type()) {
		case BOOLEAN:
			statement.setBoolean(parameterIndex, v.booleanValue());
			break;
		case STRING:
			statement.setString(parameterIndex, v.stringValue());
			break;
		case STRING_ARRAY:
		case OBJECT_ARRAY:
		{
			final PGobject jsonObject = new PGobject();
			jsonObject.setType("json");
			jsonObject.setValue(v.jsonValue(objectMapper));
			statement.setObject(parameterIndex, jsonObject);
			break;
		}
		default:
			throw new UnsupportedOperationException("Unrecognized property type: " + v.typeInfo());
		}

	}

	private FsPath dbPath()
	{
		return filesystem.systemBucket().segment("database");
	}

	private FsPath logfilePath()
	{
		return filesystem.systemBucket().segment("postgres.log");
	}

	private Connection connect() throws SQLException
	{
		final String path = String.format("jdbc:postgresql://localhost:%d/postgres", config.postgresPort());
		return DriverManager.getConnection(path, "giles", null);
	}

	private String toSqlTableName(final String tableName)
	{
		OtherPreconditions.checkNotNullOrEmpty(tableName);
		final String sqlName = tableName.replace('-', '_');
		if (!validSqlTableName(sqlName))
		{
			throw new IllegalArgumentException("Invalid table name: " + tableName);
		}
		return sqlName;
	}

	private String fromSqlTableName(final String sqlName)
	{
		OtherPreconditions.checkNotNullOrEmpty(sqlName);
		return sqlName.replace('_', '-');
	}

	@Override
	public List<String> listTableNames()
	{
		return listRawTableNames()
				.map(this::fromSqlTableName)
				.toList();
	}

	private AntiIterator<String> listRawTableNames()
	{
		final String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema='public'";
		return consumer -> {
			try (final Connection db = connect())
			{
				try (PreparedStatement statement = db.prepareStatement(sql))
				{
					try (ResultSet resultSet = statement.executeQuery())
					{
						while (resultSet.next())
						{
							final String tableName = resultSet.getString(1);
							consumer.accept(tableName);
						}
					}
				}
			}
			catch (final SQLException e)
			{
				throw new SqlServiceException(e);
			}
		};
	}

	private void stop(final File postgresDir)
	{
		try
		{
			final Process process = new ProcessBuilder("pg_ctl", "stop", "-D", postgresDir.getAbsolutePath()).start();
			final int exitCode = process.waitFor();
			if (exitCode != 0)
			{
				throw new SqlServiceException("pg_ctl stop returned exit code " + exitCode);
			}
		}
		catch (IOException | InterruptedException e)
		{
			throw new SqlServiceException(e);
		}
	}

	private void restart(final File postgresDir, final File logfile, final int port)
	{
		final String options = "-p " + port;
		try
		{
			final Process process = new ProcessBuilder(
					"pg_ctl",
					"restart",
					"-w",
					"-D", postgresDir.getAbsolutePath(),
					"-o", options,
					"-l", logfile.getAbsolutePath()).start();
			final int exitCode = process.waitFor();
			if (exitCode != 0)
			{
				throw new SqlServiceException("pg_ctl restart returned exit code " + exitCode);
			}
		}
		catch (IOException | InterruptedException e)
		{
			throw new SqlServiceException(e);
		}
	}

	private void initdb(final File postgresDir)
	{
		try
		{
			final Process process = new ProcessBuilder("pg_ctl", "initdb", "-D", postgresDir.getAbsolutePath()).start();
			final int exitCode = process.waitFor();
			if (exitCode != 0)
			{
				throw new SqlServiceException("pg_ctl initdb returned exit code " + exitCode);
			}
		}
		catch (IOException | InterruptedException e)
		{
			throw new SqlServiceException(e);
		}
	}

	@Override
	public JsonNode rsToJsonNode(final ResultSet resultSet, final List<SqlProperty> columns)
	{
		try
		{
			final ObjectNode result = new ObjectNode(objectMapper.getNodeFactory());
			for (int i = 0; i < columns.size(); i++)
			{
				final SqlProperty column = columns.get(i);
				final String fieldName = column.name();
				switch (column.typeInfo().type()) {
				case BOOLEAN:
					result.put(fieldName, resultSet.getBoolean(i + 1));
					break;
				case STRING:
					result.put(fieldName, resultSet.getString(i + 1));
					break;
				case STRING_ARRAY:
				case OBJECT_ARRAY:
					result.replace(fieldName, objectMapper.readValue(resultSet.getString(i + 1), ArrayNode.class));
					break;
				default:
					throw new UnsupportedOperationException("Cannot convert type to json: " + column.typeInfo());
				}
			}
			return result;
		}
		catch (final SQLException | IOException e)
		{
			throw new SqlBackendException(e);
		}
	}

	@Override
	public Map<String, GenericPropertyValue> rsToGenericValues(
			final ResultSet rs,
			final List<SqlProperty> columns)
	{
		try
		{
			final ImmutableMap.Builder<String, GenericPropertyValue> builder = ImmutableMap.builder();

			for (int i = 0; i < columns.size(); i++)
			{
				final SqlProperty column = columns.get(i);
				final String name = column.name();

				switch (column.typeInfo().type()) {
				case STRING:
					builder.put(name, sharedFactory.stringValue(name, rs.getString(i + 1)));
					break;
				case BOOLEAN:
					builder.put(name, sharedFactory.booleanValue(name, rs.getBoolean(i + 1)));
					break;
				case STRING_ARRAY:
				{
					final List<String> value = objectMapper.readValue(rs.getString(i + 1),
							new TypeReference<List<String>>() {
							});
					builder.put(name, sharedFactory.stringArrayValue(name, value));
					break;
				}
				case OBJECT_ARRAY:
				{
					final ArrayNode jsonNode = objectMapper.readValue(rs.getString(i + 1), ArrayNode.class);
					builder.put(name, sharedFactory.objectArrayValue(name, column.typeInfo(), jsonNode));
					break;
				}
				default:
					throw new UnsupportedOperationException("Cannot convert type from sql: " + column.typeInfo());
				}
			}
			return builder.build();
		}
		catch (final SQLException | IOException e)
		{
			throw new SqlServiceException(e);
		}
	}
}
