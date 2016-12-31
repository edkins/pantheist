package io.pantheist.api.entity.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ApiEntityModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ApiEntityModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ApiEntity.class, ApiEntityImpl.class)
				.implement(ApiComponent.class, ApiComponentImpl.class)
				.implement(ListComponentItem.class, ListComponentItemImpl.class)
				.implement(ListComponentResponse.class, ListComponentResponseImpl.class)
				.implement(ListEntityItem.class, ListEntityItemImpl.class)
				.implement(ListEntityResponse.class, ListEntityResponseImpl.class)
				.build(ApiEntityModelFactory.class));
	}

}
