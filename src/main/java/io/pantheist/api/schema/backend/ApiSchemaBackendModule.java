package io.pantheist.api.schema.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class ApiSchemaBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(SchemaBackend.class);
		bind(SchemaBackend.class).to(SchemaBackendImpl.class).in(Scopes.SINGLETON);
	}

}
