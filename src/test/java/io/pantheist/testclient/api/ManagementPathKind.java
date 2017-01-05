package io.pantheist.testclient.api;

import io.pantheist.api.kind.model.ApiKind;
import io.pantheist.api.kind.model.ListEntityResponse;
import io.pantheist.common.api.model.ListClassifierResponse;

public interface ManagementPathKind
{
	ApiKind getKind();

	ManagementData data();

	String url();

	ListEntityResponse listEntities();

	ListClassifierResponse listClassifiers();

	String urlOfService(String classifierSegment);

	/**
	 * Posts the given string to the "create" action, with the given mime type.
	 *
	 * Returns the Location header, representing the url of the created resource.
	 */
	String postCreate(String data, String contentType);

	ResponseType postCreateResponseType(String data, String contentType);
}
