package io.pantheist.api.kind.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.CreateAction;

public interface ApiKindModelFactory
{
	ListKindItem listKindItem(
			@Assisted("url") String url,
			@Assisted("name") String name,
			@Assisted("kindUrl") String kindUrl);

	ListKindResponse listKindResponse(
			List<ListKindItem> childResources,
			CreateAction createAction);

	ListEntityItem listEntityItem(
			@Assisted("url") String url,
			@Assisted("entityId") String entityId,
			@Assisted("kindUrl") String kindUrl);

	ListEntityResponse listEntityResponse(List<ListEntityItem> childResources);
}
