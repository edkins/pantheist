package io.pantheist.testclient.api;

import io.pantheist.api.kind.model.ListEntityResponse;

public interface ManagementPathEntities
{

	ListEntityResponse listEntities();

	ManagementPathUnknownEntity entity(String entityId);

	String url();

}
