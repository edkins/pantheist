package io.pantheist.testclient.api;

public interface ManagementPathJavaFile
{
	String getJava();

	void putJavaResource(String code);

	ResponseType putJavaResourceResponseType(String code);

	String url();

	String headKindUrl();

	ResponseType getJavaFileResponseType();

	void delete();

	ResponseType deleteResponseType();
}
