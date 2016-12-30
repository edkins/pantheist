package restless.api.java.resource;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class ApiJavaResourceModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(JavaResource.class);
		bind(JavaResource.class).to(JavaResourceImpl.class).in(Scopes.SINGLETON);
	}

}
