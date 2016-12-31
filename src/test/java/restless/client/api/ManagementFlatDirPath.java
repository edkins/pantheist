package restless.client.api;

import restless.api.flatdir.model.ListFileResponse;
import restless.common.api.model.ListClassifierResponse;

public interface ManagementFlatDirPath
{
	ListFileResponse listFlatDirFiles();

	ListClassifierResponse listClassifiers();

	String urlOfService(String classifierSegment);

	ResponseType listClassifierResponseType();
}
