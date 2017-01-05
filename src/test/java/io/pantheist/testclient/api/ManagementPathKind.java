package io.pantheist.testclient.api;

import io.pantheist.api.kind.model.ListEntityResponse;
import io.pantheist.handler.kind.model.Kind;

public interface ManagementPathKind
{
	Kind getKind();

	void putKindResource(String resourcePath);

	ResponseType putKindResourceResponseType(String resourcePath);

	String url();

	ListEntityResponse listEntities();

	String urlOfService(String classifierSegment);

	/**
	 * Posts the given string to the "create" action, with the given mime type.
	 *
	 * Returns the Location header, representing the url of the created resource.
	 */
	String postCreate(String data, String contentType);

	ResponseType postCreateResponseType(String data, String contentType);

	String headKindUrl();
}
