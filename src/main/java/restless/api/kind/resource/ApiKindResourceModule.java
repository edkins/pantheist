package restless.api.kind.resource;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class ApiKindResourceModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(KindResource.class);
		bind(KindResource.class).to(KindResourceImpl.class).in(Scopes.SINGLETON);
	}

}
