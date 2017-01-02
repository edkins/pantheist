package io.pantheist.common.shared.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

public final class CommonSharedModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(CommonSharedModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(GenericPropertyValue.class, Names.named("boolean"), GenericPropertyValueBooleanImpl.class)
				.implement(GenericPropertyValue.class, Names.named("string"), GenericPropertyValueStringImpl.class)
				.implement(GenericPropertyValue.class, Names.named("arrayString"),
						GenericPropertyValueArrayStringImpl.class)
				.implement(GenericProperty.class, GenericPropertyImpl.class)
				.implement(TypeInfo.class, TypeInfoImpl.class)
				.build(CommonSharedModelFactory.class));
	}

}
