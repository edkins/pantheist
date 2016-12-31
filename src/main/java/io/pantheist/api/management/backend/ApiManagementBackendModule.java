package io.pantheist.api.management.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class ApiManagementBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ManagementBackend.class);
		bind(ManagementBackend.class).to(ManagementBackendImpl.class).in(Scopes.SINGLETON);
	}

}
