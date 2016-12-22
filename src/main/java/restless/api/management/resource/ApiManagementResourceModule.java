package restless.api.management.resource;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class ApiManagementResourceModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ManagementResource.class);
		bind(ManagementResource.class).to(ManagementResourceImpl.class).in(Scopes.SINGLETON);
	}

}
