package io.pantheist.testclient.api;

import com.fasterxml.jackson.databind.JsonNode;

public interface ManagementPathUnknownEntity
{
	ResponseType getResponseTypeForContentType(String mimeType);

	JsonNode getJsonNode();

	String url();

	void add(String addName);

	ResponseType addResponseType(String addName);
}
