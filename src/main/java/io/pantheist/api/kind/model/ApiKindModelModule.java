package io.pantheist.api.kind.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ApiKindModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ApiKindModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ListKindItem.class, ListKindItemImpl.class)
				.implement(ListKindResponse.class, ListKindResponseImpl.class)
				.build(ApiKindModelFactory.class));
	}

}
