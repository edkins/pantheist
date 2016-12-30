package restless.client.api;

import restless.api.java.model.ApiJavaFile;

public interface ManagementPathJavaFile
{
	ManagementData data();

	String url();

	ApiJavaFile describeJavaFile();

	ResponseType getJavaFileResponseType();
}
