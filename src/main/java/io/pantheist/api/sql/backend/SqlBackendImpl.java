package io.pantheist.api.sql.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.pantheist.api.sql.model.ApiSqlModelFactory;
import io.pantheist.api.sql.model.ApiSqlRow;
import io.pantheist.api.sql.model.ListRowItem;
import io.pantheist.api.sql.model.ListRowResponse;
import io.pantheist.api.sql.model.ListSqlTableItem;
import io.pantheist.api.sql.model.ListSqlTableResponse;
import io.pantheist.common.api.model.CommonApiModelFactory;
import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.shared.model.TypeInfo;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.sql.backend.SqlService;
import io.pantheist.handler.sql.model.SqlProperty;

final class SqlBackendImpl implements SqlBackend
{
	private final SqlService sqlService;
	private final ApiSqlModelFactory modelFactory;
	private final UrlTranslation urlTranslation;
	private final CommonApiModelFactory commonFactory;
	private final KindStore kindStore;
	private final ObjectMapper objectMapper;

	@Inject
	private SqlBackendImpl(
			final SqlService sqlStore,
			final ApiSqlModelFactory modelFactory,
			final UrlTranslation urlTranslation,
			final CommonApiModelFactory commonFactory,
			final KindStore kindStore,
			final ObjectMapper objectMapper)
	{
		this.sqlService = checkNotNull(sqlStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.commonFactory = checkNotNull(commonFactory);
		this.kindStore = checkNotNull(kindStore);
		this.objectMapper = checkNotNull(objectMapper);
	}

	private ListSqlTableItem toListSqlTableItem(final String tableName)
	{
		return modelFactory.listSqlTableItem(
				urlTranslation.sqlTableToUrl(tableName),
				tableName,
				urlTranslation.kindToUrl("sql-table"));
	}

	@Override
	public ListSqlTableResponse listSqlTables()
	{
		return AntiIt.from(sqlService.listTableNames())
				.map(this::toListSqlTableItem)
				.wrap(modelFactory::listSqlTableResponse);
	}

	private ListClassifierItem toListClassifierItem(final String table, final String column)
	{
		return commonFactory.listClassifierItem(
				urlTranslation.sqlTableColumnToUrl(table, column),
				column, false, urlTranslation.kindToUrl("pantheist-classifier"));
	}

	@Override
	public Possible<ListClassifierResponse> listSqlTableClassifiers(final String table)
	{
		if (sqlService.listTableNames().contains(table))
		{
			return View.ok(kindStore.listSqlPropertiesOfKind(table)
					.filter(p -> p.isKey())
					.map(p -> toListClassifierItem(table, p.name()))
					.wrap(commonFactory::listClassifierResponse));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	private ListRowItem rsToListRowItem(final String table, final String column, final ResultSet resultSet)
	{
		try
		{
			final String value = resultSet.getString(1);
			return modelFactory.listRowItem(
					urlTranslation.sqlRowToUrl(table, column, value),
					urlTranslation.kindToUrl("sql-row"));
		}
		catch (final SQLException e)
		{
			throw new SqlBackendException(e);
		}
	}

	private Optional<TypeInfo> lookupColumn(final String table, final String column)
	{
		return kindStore.listSqlPropertiesOfKind(table)
				.filter(p -> p.name().equals(column))
				.failIfMultiple()
				.filter(p -> p.isKey())
				.map(p -> p.typeInfo());
	}

	@Override
	public Possible<ListRowResponse> listRows(final String table, final String column)
	{
		if (lookupColumn(table, column).isPresent())
		{
			return View.ok(sqlService.selectAllRows(table, ImmutableList.of(column))
					.map(rs -> rsToListRowItem(table, column, rs))
					.wrap(modelFactory::listRowResponse));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	private JsonNode parseValue(final TypeInfo type, final String valueString)
	{
		final JsonNodeFactory nf = objectMapper.getNodeFactory();
		switch (type.type()) {
		case STRING:
			return nf.textNode(valueString);
		case BOOLEAN:
		case ARRAY:
		case OBJECT:
		default:
			throw new UnsupportedOperationException("Cannot parse type " + type);
		}
	}

	@Override
	public Possible<ApiSqlRow> getRowInfo(final String table, final String column, final String row)
	{
		final Optional<TypeInfo> columnType = lookupColumn(table, column);
		if (!columnType.isPresent())
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
		final JsonNode index = parseValue(columnType.get(), row);
		final Optional<ApiSqlRow> result = sqlService
				.selectIndividualRow(table, column, index, ImmutableList.of(column))
				.failIfMultiple()
				.map(x -> modelFactory.sqlRow(urlTranslation.sqlRowDataAction(table, column, row)));

		if (result.isPresent())
		{
			return View.ok(result.get());
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<ObjectNode> getRowData(final String table, final String column, final String row)
	{
		final Optional<TypeInfo> columnType = lookupColumn(table, column);
		if (!columnType.isPresent())
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}

		final List<SqlProperty> columns = kindStore.listSqlPropertiesOfKind(table).toList();
		if (columns.isEmpty())
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}

		final List<String> columnNames = Lists.transform(columns, SqlProperty::name);
		final JsonNode index = parseValue(columnType.get(), row);

		return sqlService.selectIndividualRow(table, column, index, columnNames)
				.map(rs -> sqlService.rsToJsonNode(rs, columns))
				.failIfMultiple()
				.map(View::ok)
				.orElse(FailureReason.DOES_NOT_EXIST.happened());
	}

}
