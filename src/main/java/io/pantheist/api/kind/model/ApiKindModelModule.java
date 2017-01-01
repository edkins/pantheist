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
				.implement(ApiKind.class, ApiKindImpl.class)
				.implement(ListKindResponse.class, ListKindResponseImpl.class)
				.implement(ListKindItem.class, ListKindItemImpl.class)
				.implement(ListEntityResponse.class, ListEntityResponseImpl.class)
				.implement(ListEntityItem.class, ListEntityItemImpl.class)
				.build(ApiKindModelFactory.class));
	}

}
