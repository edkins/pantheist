package io.pantheist.handler.sql.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.api.sql.backend.SqlBackendException;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.common.util.Pair;
import io.pantheist.handler.sql.model.SqlProperty;

final class SelectBuilderImpl implements SelectBuilder
{
	private static final Logger LOGGER = LogManager.getLogger(SelectBuilderImpl.class);
	private final String tableName;
	private final SqlCoreService coreService;
	private final ObjectMapper objectMapper;
	private final List<SqlProperty> columns;
	private final List<Pair<String, JsonNode>> whereClauses;

	SelectBuilderImpl(final SqlCoreService coreService,
			final ObjectMapper objectMapper,
			final String tableName,
			final List<SqlProperty> columns)
	{
		this.coreService = checkNotNull(coreService);
		this.objectMapper = checkNotNull(objectMapper);

		this.tableName = OtherPreconditions.checkNotNullOrEmpty(tableName);
		this.columns = new ArrayList<>(columns);
		this.whereClauses = new ArrayList<>();
	}

	@Override
	public SelectBuilder whereEqual(final String column, final JsonNode value)
	{
		whereClauses.add(Pair.of(column, value.deepCopy()));
		return this;
	}

	@Override
	public SelectBuilder columns(final Collection<String> columnNames)
	{
		columns.removeIf(p -> !columnNames.contains(p.name()));
		return this;
	}

	private String statement()
	{
		final String sqlTableName = coreService.toSqlTableName(tableName);
		final String sqlColumnNames = AntiIt.from(columns)
				.map(p -> coreService.toSqlColumnName(p.name())).join(",")
				.orElse("1");

		if (whereClauses.isEmpty())
		{
			return String.format("SELECT %s FROM %s", sqlColumnNames, sqlTableName);
		}
		else
		{
			final String sqlWhereClauses = AntiIt.from(whereClauses)
					.map(this::toSqlWhereClause)
					.join(",")
					.get();

			return String.format("SELECT %s FROM %s WHERE %s", sqlColumnNames, sqlTableName, sqlWhereClauses);
		}
	}

	private String toSqlWhereClause(final Pair<String, JsonNode> whereClause)
	{
		return String.format(" %s = ? ", coreService.toSqlColumnName(whereClause.first()));
	}

	@Override
	public AntiIterator<ObjectNode> execute()
	{
		return consumer -> {
			try (Connection db = coreService.connect())
			{
				final String sql = statement();
				LOGGER.info("sql = {}", sql);
				try (PreparedStatement statement = db.prepareStatement(sql))
				{
					for (int i = 0; i < whereClauses.size(); i++)
					{
						coreService.setStatementValue(db, statement, i + 1, whereClauses.get(i).second());
					}
					try (ResultSet rs = statement.executeQuery())
					{
						while (rs.next())
						{
							consumer.accept(rsToJsonNode(rs));
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

	private ObjectNode rsToJsonNode(final ResultSet resultSet)
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
				case ARRAY:
				case OBJECT:
					result.replace(fieldName, objectMapper.readValue(resultSet.getString(i + 1), JsonNode.class));
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
}
