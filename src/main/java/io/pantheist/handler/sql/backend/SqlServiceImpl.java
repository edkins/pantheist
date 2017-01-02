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
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import io.pantheist.common.shared.model.GenericProperty;
import io.pantheist.common.shared.model.GenericPropertyValue;
import io.pantheist.common.shared.model.PropertyType;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.system.config.PantheistConfig;

final class SqlServiceImpl implements SqlService
{
	private static final Logger LOGGER = LogManager.getLogger(SqlServiceImpl.class);
	private final FilesystemStore filesystem;

	private final PantheistConfig config;

	private final Pattern WORD = Pattern.compile("[a-z][a-z_]*");

	private final Set<String> reservedWordsLowercase;

	@Inject
	private SqlServiceImpl(final FilesystemStore filesystem, final PantheistConfig config)
	{
		this.filesystem = checkNotNull(filesystem);
		this.config = checkNotNull(config);
		this.reservedWordsLowercase = new HashSet<>();
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
				db.prepareStatement(sql).execute();
			}
		}
		catch (final SQLException e)
		{
			throw new SqlServiceException(e);
		}
	}

	private String toSqlCreateTableArg(final GenericProperty property)
	{
		final String sqlName = toSqlColumnName(property.name());

		if (property.isIdentifier())
		{
			return String.format("%s %s PRIMARY KEY", sqlName, property.type().sql());
		}
		else
		{
			return String.format("%s %s", sqlName, property.type().sql());
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
	public void createTable(final String tableName, final List<GenericProperty> columns)
	{
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
	public Optional<PropertyType> getColumnType(final String table, final String columnName)
	{
		final String sql = "SELECT data_type FROM information_schema.columns WHERE table_name = ? AND column_name = ?";
		final String sqlTableName = toSqlTableName(table);
		final String sqlColumnName = toSqlColumnName(columnName);

		try (final Connection db = connect())
		{
			try (PreparedStatement statement = db.prepareStatement(sql))
			{
				statement.setString(1, sqlTableName);
				statement.setString(2, sqlColumnName);
				try (ResultSet resultSet = statement.executeQuery())
				{
					if (resultSet.next())
					{
						return Optional.of(sqlTypeNameToPropertyType(resultSet.getString(1)));
					}
					else
					{
						return Optional.empty();
					}
				}
			}
		}
		catch (final SQLException e)
		{
			throw new SqlServiceException(e);
		}
	}

	private PropertyType sqlTypeNameToPropertyType(final String string)
	{
		switch (string) {
		case "character varying":
			return PropertyType.STRING;
		default:
			throw new IllegalStateException("Unknown column type: " + string);
		}
	}

	@Override
	public AntiIterator<ResultSet> selectIndividualRow(final String tableName, final GenericPropertyValue indexValue,
			final ImmutableList<String> columnNames)
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
					setStatementValue(statement, 1, indexValue);
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
	public void updateOrInsert(final String tableName, final List<GenericPropertyValue> values)
	{
		if (values.isEmpty())
		{
			throw new IllegalArgumentException("List of values must be nonempty");
		}
		final String columnSql = AntiIt.from(values).map(v -> toSqlColumnName(v.name())).join(",").get();
		final String questionMarks = AntiIt.from(values).map(v -> "?").join(",").get();
		final String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
				toSqlTableName(tableName),
				columnSql,
				questionMarks);
		try (final Connection db = connect())
		{
			try (PreparedStatement statement = db.prepareStatement(sql))
			{
				setUpStatement(statement, values);
				statement.execute();
			}
		}
		catch (final SQLException e)
		{
			throw new SqlServiceException(e);
		}
	}

	private void setUpStatement(final PreparedStatement statement, final List<GenericPropertyValue> values)
			throws SQLException
	{
		for (int i = 0; i < values.size(); i++)
		{
			final GenericPropertyValue v = values.get(i);
			setStatementValue(statement, i + 1, v);
		}
	}

	private void setStatementValue(final PreparedStatement statement, final int parameterIndex,
			final GenericPropertyValue v)
			throws SQLException
	{
		switch (v.type()) {
		case BOOLEAN:
			statement.setBoolean(parameterIndex, v.booleanValue());
			break;
		case STRING:
			statement.setString(parameterIndex, v.stringValue());
			break;
		default:
			throw new UnsupportedOperationException("Unrecognized property type: " + v.type());
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
	public List<String> listTableIdentifiers(final String tableName)
	{
		final String sqlTableName = toSqlTableName(tableName);
		final String sql = "SELECT column_name FROM information_schema.key_column_usage where table_name=?";
		final ImmutableList.Builder<String> builder = ImmutableList.builder();
		try (final Connection db = connect())
		{
			try (PreparedStatement statement = db.prepareStatement(sql))
			{
				statement.setString(1, sqlTableName);
				try (ResultSet resultSet = statement.executeQuery())
				{
					while (resultSet.next())
					{
						final String columnName = resultSet.getString(1);
						builder.add(columnName);
					}
				}
			}
			return builder.build();
		}
		catch (final SQLException e)
		{
			throw new SqlServiceException(e);
		}
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
}
