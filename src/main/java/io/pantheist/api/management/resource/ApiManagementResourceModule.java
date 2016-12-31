package io.pantheist.api.management.resource;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import io.pantheist.common.annotations.ResourceTag;

public class ApiManagementResourceModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		final Multibinder<ResourceTag> multi = Multibinder.newSetBinder(binder(), ResourceTag.class);
		multi.addBinding().to(ManagementResource.class);
	}

}
