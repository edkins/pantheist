package io.pantheist.api.entity.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

public interface ApiEntityModelFactory
{
	ListEntityItem listEntityItem(
			@Assisted("url") String url,
			@Assisted("entityId") String entityId,
			@Assisted("kindUrl") String kindUrl);

	ListEntityResponse listEntityResponse(List<ListEntityItem> childResources);
}
