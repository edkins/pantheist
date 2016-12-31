package io.pantheist.api.flatdir.resource;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import io.pantheist.common.annotations.ResourceTag;

public class ApiFlatDirResourceModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		final Multibinder<ResourceTag> multi = Multibinder.newSetBinder(binder(), ResourceTag.class);
		multi.addBinding().to(FlatDirResource.class);
	}

}
