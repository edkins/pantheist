package io.pantheist.testclient.api;

import io.pantheist.common.api.model.ListClassifierResponse;

public interface ManagementPathSqlTable
{
	ListClassifierResponse listClassifiers();

	ResponseType listClassifierResponseType();

	String urlOfService(String classifierSegment);
}
