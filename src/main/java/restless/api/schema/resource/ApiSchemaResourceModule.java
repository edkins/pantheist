package restless.api.schema.resource;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import restless.common.annotations.ResourceTag;

public class ApiSchemaResourceModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		final Multibinder<ResourceTag> multi = Multibinder.newSetBinder(binder(), ResourceTag.class);
		multi.addBinding().to(SchemaResource.class);
	}

}
