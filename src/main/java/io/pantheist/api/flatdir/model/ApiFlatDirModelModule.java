package io.pantheist.api.flatdir.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ApiFlatDirModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ApiFlatDirModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ListFileItem.class, ListFileItemImpl.class)
				.implement(ListFileResponse.class, ListFileResponseImpl.class)
				.implement(ListFlatDirItem.class, ListFlatDirItemImpl.class)
				.implement(ListFlatDirResponse.class, ListFlatDirResponseImpl.class)
				.build(ApiFlatDirModelFactory.class));
	}

}
