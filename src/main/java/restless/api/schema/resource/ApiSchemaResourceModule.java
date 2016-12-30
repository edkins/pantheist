package restless.api.schema.resource;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class ApiSchemaResourceModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(SchemaResource.class);
		bind(SchemaResource.class).to(SchemaResourceImpl.class).in(Scopes.SINGLETON);
	}

}
