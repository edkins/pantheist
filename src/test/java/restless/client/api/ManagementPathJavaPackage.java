package restless.client.api;

import restless.api.java.model.ListJavaFileResponse;
import restless.common.api.model.ListClassifierResponse;

public interface ManagementPathJavaPackage
{
	/**
	 * Return the java file with a particular name in this package.
	 */
	ManagementPathJavaFile file(String file);

	String urlOfService(String classifierSegment);

	ListClassifierResponse listClassifiers();

	ResponseType listClassifierResponseType();

	String url();

	ListJavaFileResponse listJavaFiles();
}
