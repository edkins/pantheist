package restless.api.management.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ApiManagementModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ApiManagementModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ListConfigItem.class, ListConfigItemImpl.class)
				.implement(ListConfigResponse.class, ListConfigResponseImpl.class)
				.implement(ApiEntity.class, ApiEntityImpl.class)
				.build(ApiManagementModelFactory.class));
	}

}
