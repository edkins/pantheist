package io.pantheist.handler.sql.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.pantheist.api.sql.backend.SqlBackendException;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.FilterableObjectStream;
import io.pantheist.common.util.OtherPreconditions;
import io.pantheist.common.util.Pair;
import io.pantheist.handler.sql.model.SqlProperty;

final class SelectBuilderImpl implements FilterableObjectStream
{
	private static final Logger LOGGER = LogManager.getLogger(SelectBuilderImpl.class);
	private final String tableName;
	private final SqlCoreService coreService;
	private final ObjectMapper objectMapper;
	private final List<SqlProperty> columns;
	private final List<Pair<String, JsonNode>> whereClauses;
	private Predicate<ObjectNode> postFilter;
	private final Set<String> touchedFields;

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
		this.postFilter = x -> true;
		this.touchedFields = new HashSet<>();
	}

	@Override
	public FilterableObjectStream whereEqual(final String field, final JsonNode value)
	{
		if (touchedFields.contains(field))
		{
			throw new IllegalStateException("whereEqual can't be called on a field set with setField");
		}
		whereClauses.add(Pair.of(field, value.deepCopy()));
		return this;
	}

	@Override
	public FilterableObjectStream fields(final Collection<String> fieldNames)
	{
		if (!touchedFields.isEmpty())
		{
			throw new IllegalStateException("fields can't be called after setField");
		}
		columns.removeIf(p -> !fieldNames.contains(p.name()));
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
	public AntiIterator<ObjectNode> antiIt()
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
							final ObjectNode obj = rsToJsonNode(rs);
							if (postFilter.test(obj))
							{
								consumer.accept(obj);
							}
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

	@Override
	public FilterableObjectStream setField(final String fieldName, final Function<ObjectNode, JsonNode> fn)
	{
		OtherPreconditions.checkNotNullOrEmpty(fieldName);
		touchedFields.add(fieldName);
		final Predicate<ObjectNode> oldFilter = postFilter;
		postFilter = obj -> {
			if (!oldFilter.test(obj))
			{
				return false;
			}
			obj.set(fieldName, fn.apply(obj));
			return true;
		};
		return this;
	}

	@Override
	public FilterableObjectStream postFilter(final Predicate<ObjectNode> predicate)
	{
		postFilter = postFilter.and(predicate);
		return this;
	}
}
