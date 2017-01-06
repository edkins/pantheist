package io.pantheist.testclient.api;

public interface ManagementPathUnknownEntity
{
	ResponseType getResponseTypeForContentType(String mimeType);

	String url();
}
