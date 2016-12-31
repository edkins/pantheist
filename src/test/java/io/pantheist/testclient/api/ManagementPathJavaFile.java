package io.pantheist.testclient.api;

import io.pantheist.api.java.model.ApiJavaFile;

public interface ManagementPathJavaFile
{
	ManagementData data();

	String url();

	ApiJavaFile describeJavaFile();

	ResponseType getJavaFileResponseType();

	void delete();

	ResponseType deleteResponseType();
}
