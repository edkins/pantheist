package io.pantheist.testclient.api;

import io.pantheist.handler.kind.model.Kind;

public interface ManagementPathKind
{
	Kind getKind();

	void putKindResource(String resourcePath);

	void putKindString(String text);

	ResponseType putKindResourceResponseType(String resourcePath);

	String url();

	String urlOfService(String classifierSegment);

	/**
	 * Posts the given string to the "create" action, with the given mime type.
	 *
	 * Returns the Location header, representing the url of the created resource.
	 */
	String postCreate(String data, String contentType);

	ResponseType postCreateResponseType(String data, String contentType);

	String headKindUrl();

	/**
	 * Posts an empty request to the "new" action, which is like the create action
	 * but does not require data.
	 *
	 * This action responds with a url, which we wrap in a ManagementPathUnknownEntity
	 * since we don't know at compile time what kind of entity this is referring to.
	 */
	ManagementPathUnknownEntity postNew();
}
