package restless.api.kind.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class ApiKindBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(KindBackend.class);
		bind(KindBackend.class).to(KindBackendImpl.class).in(Scopes.SINGLETON);
	}

}
