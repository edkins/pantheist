package io.pantheist.api.management.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.ListClassifierItem;

public interface ApiManagementModelFactory
{
	ListConfigItem listConfigItem(@Assisted("url") String url);

	ListConfigResponse listConfigResponse(List<ListConfigItem> childResources);

	ListRootResponse listRootResponse(
			List<ListClassifierItem> childResources,
			@Assisted("clientConfigUrl") String clientConfigUrl);
}
