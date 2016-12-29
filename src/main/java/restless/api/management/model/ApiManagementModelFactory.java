package restless.api.management.model;

import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

public interface ApiManagementModelFactory
{
	ListConfigItem listConfigItem(@Assisted("url") String url);

	ListConfigResponse listConfigResponse(List<ListConfigItem> childResources);

	ApiEntity entity(
			@Nullable @Assisted("jsonSchemaUrl") final String jsonSchemaUrl,
			@Nullable @Assisted("javaUrl") final String javaUrl);
}
