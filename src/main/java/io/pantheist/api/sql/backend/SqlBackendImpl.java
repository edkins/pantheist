package io.pantheist.api.sql.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;

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
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.kind.backend.KindStore;
import io.pantheist.handler.kind.model.Kind;
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

	private ListSqlTableItem toListSqlTableItem(final Kind kind)
	{
		return modelFactory.listSqlTableItem(
				urlTranslation.sqlTableToUrl(kind.kindId()),
				kind.kindId(),
				urlTranslation.kindToUrl("sql-table"));
	}

	@Override
	public ListSqlTableResponse listSqlTables()
	{
		return kindStore.listAllKinds()
				.filter(Kind::shouldRegisterInSql)
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
		final List<ListClassifierItem> list = kindStore.listSqlPropertiesOfKind(table)
				.filter(p -> p.isKey())
				.map(p -> toListClassifierItem(table, p.name()))
				.toList();
		if (list.isEmpty())
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
		else
		{
			return View.ok(commonFactory.listClassifierResponse(list));
		}
	}

	private ListRowItem rsToListRowItem(final String table, final String column, final JsonNode value)
	{
		return modelFactory.listRowItem(
				urlTranslation.sqlRowToUrl(table, column, value.textValue()),
				urlTranslation.kindToUrl("sql-row"));
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
			final List<SqlProperty> columns = kindStore.listSqlPropertiesOfKind(table).toList();
			return View.ok(sqlService.select(table, columns)
					.fields(ImmutableList.of(column))
					.antiIt()
					.map(obj -> rsToListRowItem(table, column, obj.get(column)))
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

		final List<SqlProperty> columns = kindStore.listSqlPropertiesOfKind(table).toList();
		final JsonNode index = parseValue(columnType.get(), row);
		final Optional<ApiSqlRow> result = sqlService
				.select(table, columns)
				.whereEqual(column, index)
				.antiIt()
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
		final JsonNode index = parseValue(columnType.get(), row);

		return sqlService.select(table, columns)
				.whereEqual(column, index)
				.antiIt()
				.failIfMultiple()
				.map(View::ok)
				.orElse(FailureReason.DOES_NOT_EXIST.happened());
	}

}
