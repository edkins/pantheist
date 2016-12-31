package io.pantheist.api.java.resource;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import io.pantheist.common.annotations.ResourceTag;

public class ApiJavaResourceModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		final Multibinder<ResourceTag> multi = Multibinder.newSetBinder(binder(), ResourceTag.class);
		multi.addBinding().to(JavaResource.class);
	}

}
