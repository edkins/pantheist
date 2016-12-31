package io.pantheist.testclient.api;

import io.pantheist.api.flatdir.model.ListFileResponse;
import io.pantheist.common.api.model.ListClassifierResponse;

public interface ManagementFlatDirPath
{
	ListFileResponse listFlatDirFiles();

	ListClassifierResponse listClassifiers();

	String urlOfService(String classifierSegment);

	ResponseType listClassifierResponseType();
}
