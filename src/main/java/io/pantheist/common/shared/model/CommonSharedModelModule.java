package io.pantheist.common.shared.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public final class CommonSharedModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(CommonSharedModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(TypeInfo.class, TypeInfoImpl.class)
				.build(CommonSharedModelFactory.class));
	}

}
