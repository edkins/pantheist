package io.pantheist.api.schema.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.CreateAction;

public interface ApiSchemaModelFactory
{
	ListSchemaResponse listSchemaResponse(
			List<ListSchemaItem> childResources,
			CreateAction createAction);

	ListSchemaItem listSchemaItem(
			@Assisted("url") String url,
			@Assisted("kindUrl") String kindUrl);
}
