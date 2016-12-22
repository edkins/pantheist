package restless.api.something.resource;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class ApiSomethingResourceModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(SomethingResource.class);
		bind(SomethingResource.class).to(SomethingResourceImpl.class).in(Scopes.SINGLETON);
	}

}
