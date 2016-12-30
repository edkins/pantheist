package restless.api.management.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.ListClassifierItem;

public interface ApiManagementModelFactory
{
	ListConfigItem listConfigItem(@Assisted("url") String url);

	ListConfigResponse listConfigResponse(List<ListConfigItem> childResources);

	ListClassifierResponse listClassifierResponse(List<ListClassifierItem> childResources);
}
