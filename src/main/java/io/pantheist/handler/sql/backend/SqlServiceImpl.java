package io.pantheist.handler.sql.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.handler.filesystem.backend.FilesystemSnapshot;
import io.pantheist.handler.filesystem.backend.FilesystemStore;
import io.pantheist.handler.filesystem.backend.FsPath;
import io.pantheist.system.config.PantheistConfig;

final class SqlServiceImpl implements SqlService
{
	private final FilesystemStore filesystem;

	private final PantheistConfig config;

	private final Pattern TABLE_NAME = Pattern.compile("[a-z][a-z_]*");

	@Inject
	private SqlServiceImpl(final FilesystemStore filesystem, final PantheistConfig config)
	{
		this.filesystem = checkNotNull(filesystem);
		this.config = checkNotNull(config);
	}

	@Override
	public void startOrRestart()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		final boolean dirExists = snapshot.isDir(dbPath());
		snapshot.isFile(logfilePath());

		snapshot.write(map -> {
			final File dbPath = map.get(dbPath());
			final File logfile = map.get(logfilePath());
			if (!dirExists)
			{
				initdb(dbPath);
			}
			restart(dbPath, logfile, config.postgresPort());
			createTableIfNotExists("java-file");
		});

		try
		{
			Class.forName("org.postgresql.Driver");
		}
		catch (final ClassNotFoundException e)
		{
			throw new SqlServiceException(e);
		}
	}

	@Override
	public void stop()
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		snapshot.isDir(dbPath());
		snapshot.writeSingle(dbPath(), this::stop);
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
		if (!TABLE_NAME.matcher(sqlName).matches())
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

	private void createTableIfNotExists(final String tableName)
	{
		final String sql = String.format("create table if not exists %s ()", toSqlTableName(tableName));

		try (final Connection db = connect())
		{
			try (PreparedStatement statement = db.prepareStatement(sql))
			{
				statement.execute();
			}
		}
		catch (final SQLException e)
		{
			throw new SqlServiceException(e);
		}
	}

	@Override
	public List<String> listTableNames()
	{
		try (final Connection db = connect())
		{
			try (PreparedStatement statement = db
					.prepareStatement("select table_name from information_schema.tables where table_schema='public'"))
			{
				try (ResultSet resultSet = statement.executeQuery())
				{
					final ImmutableList.Builder<String> builder = ImmutableList.builder();
					while (resultSet.next())
					{
						final String tableName = resultSet.getString(1);
						builder.add(fromSqlTableName(tableName));
					}
					return builder.build();
				}
			}
		}
		catch (final SQLException e)
		{
			throw new SqlServiceException(e);
		}
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
					"-o", options).start();
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
