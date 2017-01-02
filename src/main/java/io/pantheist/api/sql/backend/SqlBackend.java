package io.pantheist.api.sql.backend;

import io.pantheist.api.sql.model.ListRowResponse;
import io.pantheist.api.sql.model.ListSqlTableResponse;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.common.util.Possible;

public interface SqlBackend
{
	ListSqlTableResponse listSqlTables();

	Possible<ListClassifierResponse> listSqlTableClassifiers(String table);

	Possible<ListRowResponse> listRows(String table, String column);
}