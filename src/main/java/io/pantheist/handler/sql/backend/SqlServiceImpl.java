package io.pantheist.handler.sql.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	private static final long BATCH_SIZE = 4000;
	private final FilesystemStore filesystem;

	private final PantheistConfig config;

	private final ObjectMapper objectMapper;
	private final SqlCoreService coreService;

	@Inject
	private SqlServiceImpl(
			final FilesystemStore filesystem,
			final PantheistConfig config,
			final ObjectMapper objectMapper,
			final SqlCoreService coreService)
	{
		this.filesystem = checkNotNull(filesystem);
		this.config = checkNotNull(config);
		this.objectMapper = checkNotNull(objectMapper);
		this.coreService = checkNotNull(coreService);
	}

	@Override
	public void startOrRestart()
	{
		try
		{
			coreService.startOrRestart();

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
		try (final Connection db = coreService.connect())
		{
			for (final String sqlTableName : sqlTableNames)
			{
				if (!coreService.validSqlTableName(sqlTableName))
				{
					throw new IllegalStateException("Weird table name discovered: " + sqlTableName);
				}
				final String sql = String.format("DROP TABLE %s", sqlTableName);
				LOGGER.info("sql = {}", sql);
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
		final String sqlName = coreService.toSqlColumnName(property.name());

		if (property.isPrimaryKey())
		{
			return String.format("%s %s PRIMARY KEY NOT NULL", sqlName, property.toSqlType());
		}
		else
		{
			return String.format("%s %s NOT NULL", sqlName, property.toSqlType());
		}
	}

	@Override
	public void createTable(final String tableName, final List<SqlProperty> columns)
	{
		if (columns.stream().filter(SqlProperty::isPrimaryKey).collect(Collectors.counting()) != 1)
		{
			throw new IllegalArgumentException("Need exactly one primary key");
		}

		final String columnSql = AntiIt.from(columns).map(this::toSqlCreateTableArg).join(",").orElse("");
		final String sql = String.format("CREATE TABLE %s (%s)", coreService.toSqlTableName(tableName), columnSql);

		LOGGER.info("sql = {}", sql);
		try (final Connection db = coreService.connect())
		{
			db.prepareStatement(sql).execute();
		}
		catch (final SQLException e)
		{
			throw new SqlServiceException(e);
		}
	}

	@Override
	public void updateOrInsert(
			final String tableName,
			final List<SqlProperty> columns,
			final AntiIterator<ObjectNode> valueObjs)
	{
		final String primaryKeyColumn = AntiIt.from(columns)
				.filter(SqlProperty::isPrimaryKey)
				.map(SqlProperty::name)
				.failIfMultiple()
				.get();

		final String columnSql = AntiIt.from(columns)
				.map(p -> coreService.toSqlColumnName(p.name())).join(",").get();
		final String questionMarks = AntiIt.from(columns).map(p -> "?").join(",").get();

		// Note this will fail if only the primary key column exists
		final String doUpdateSql = AntiIt.from(columns)
				.filter(p -> !p.isPrimaryKey())
				.map(p -> coreService.toSqlColumnName(p.name()) + " = EXCLUDED."
						+ coreService.toSqlColumnName(p.name()))
				.join(",")
				.get();

		final String sql = String.format("INSERT INTO %s (%s) VALUES (%s) ON CONFLICT (%s) DO UPDATE SET %s",
				coreService.toSqlTableName(tableName),
				columnSql,
				questionMarks,
				coreService.toSqlColumnName(primaryKeyColumn),
				doUpdateSql);
		try (final Connection db = coreService.connect())
		{
			try (PreparedStatement statement = db.prepareStatement(sql))
			{
				final AtomicLong counter = new AtomicLong(0);
				valueObjs.forEach(obj -> {
					try
					{
						for (int i = 0; i < columns.size(); i++)
						{
							final JsonNode v = obj.get(columns.get(i).name());
							coreService.setStatementValue(db, statement, i + 1, v);
						}
						statement.addBatch();
						if (counter.incrementAndGet() % BATCH_SIZE == 0)
						{
							LOGGER.info("sql = {}", sql);
							statement.executeBatch();
						}
					}
					catch (final SQLException | JsonProcessingException e)
					{
						throw new SqlServiceException(e);
					}
				});
				LOGGER.info("sql = {}", sql);
				statement.executeBatch();
			}
		}
		catch (final SQLException e)
		{
			throw new SqlServiceException(e);
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

	private AntiIterator<String> listRawTableNames()
	{
		final String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema='public'";
		return consumer -> {
			try (final Connection db = coreService.connect())
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
	public SelectBuilder select(final String table, final List<SqlProperty> columns)
	{
		OtherPreconditions.checkNotNullOrEmpty(table);
		checkNotNull(columns);
		return new SelectBuilderImpl(coreService, objectMapper, table, columns);
	}
}
