package restless.api.management.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

public interface ApiManagementModelFactory
{
	ListConfigItem listConfigItem(@Assisted("url") String url);

	ListConfigResponse listConfigResponse(List<ListConfigItem> childResources);
}
