package io.pantheist.api.sql.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import io.pantheist.api.sql.model.ApiSqlModelFactory;
import io.pantheist.api.sql.model.ListSqlTableItem;
import io.pantheist.api.sql.model.ListSqlTableResponse;
import io.pantheist.common.api.model.CommonApiModelFactory;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.AntiIt;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.sql.backend.SqlService;

final class SqlBackendImpl implements SqlBackend
{
	private final SqlService sqlService;
	private final ApiSqlModelFactory modelFactory;
	private final UrlTranslation urlTranslation;
	private final CommonApiModelFactory commonFactory;

	@Inject
	private SqlBackendImpl(
			final SqlService sqlStore,
			final ApiSqlModelFactory modelFactory,
			final UrlTranslation urlTranslation,
			final CommonApiModelFactory commonFactory)
	{
		this.sqlService = checkNotNull(sqlStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.commonFactory = checkNotNull(commonFactory);
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

	@Override
	public Possible<ListClassifierResponse> listSqlTableClassifiers(final String table)
	{
		if (sqlService.listTableNames().contains(table))
		{
			return View.ok(
					commonFactory.listClassifierResponse(
							urlTranslation.listSqlTableClassifiers(table)));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

}
