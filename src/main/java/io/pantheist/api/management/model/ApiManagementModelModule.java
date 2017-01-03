package io.pantheist.api.management.model;

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
				.implement(ListRootResponse.class, ListRootResponseImpl.class)
				.build(ApiManagementModelFactory.class));
	}

}
