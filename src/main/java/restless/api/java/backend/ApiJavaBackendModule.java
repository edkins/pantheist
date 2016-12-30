package restless.api.java.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class ApiJavaBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(JavaBackend.class);
		bind(JavaBackend.class).to(JavaBackendImpl.class).in(Scopes.SINGLETON);
	}

}
