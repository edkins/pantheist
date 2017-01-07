package io.pantheist.api.entity.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public final class ApiEntityModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ApiEntityModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ListEntityItem.class, ListEntityItemImpl.class)
				.implement(ListEntityResponse.class, ListEntityResponseImpl.class)
				.build(ApiEntityModelFactory.class));
	}

}
