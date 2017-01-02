package io.pantheist.testclient.api;

import io.pantheist.api.sql.model.ListRowResponse;
import io.pantheist.common.api.model.ListClassifierResponse;

public interface ManagementPathSqlTable
{
	ListClassifierResponse listClassifiers();

	ResponseType listClassifierResponseType();

	String urlOfService(String classifierSegment);

	ListRowResponse listBy(String indexColumn);

	ResponseType listByResponseType(String indexColumn);

	ManagementPathSqlRow row(String indexColumn, String indexValue);
}
