package io.pantheist.testclient.api;

import io.pantheist.api.sql.model.ApiSqlRow;

public interface ManagementPathSqlRow
{
	String url();

	ResponseType getSqlRowResponseType();

	ApiSqlRow getSqlRow();

	ManagementData data();
}
