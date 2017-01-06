package io.pantheist.testclient.api;

public interface ManagementPathSchema
{
	ResponseType validate(String data, String contentType);

	String url();

	void putJsonSchemaResource(String resourcePath);

	void putJsonSchemaString(String text);

	ResponseType putJsonSchemaResourceResponseType(String resourcePath);

	String getJsonSchemaString();

	ResponseType getJsonSchemaResponseType();

	void delete();

	ResponseType deleteResponseType();

	String headKindUrl();
}
