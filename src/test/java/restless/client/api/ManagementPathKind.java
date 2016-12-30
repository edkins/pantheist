package restless.client.api;

import restless.api.kind.model.ApiKind;
import restless.api.kind.model.ListEntityResponse;
import restless.api.management.model.ListClassifierResponse;

public interface ManagementPathKind
{
	void putJsonResource(String resourcePath);

	ResponseType putJsonResourceResponseType(String resourcePath);

	ApiKind getKind();

	String url();

	ListEntityResponse listEntities();

	ListClassifierResponse listClassifiers();

	String urlOfService(String classifierSegment);
}
