package restless.glue.initializer;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class GlueInitializerModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(Initializer.class);
		bind(Initializer.class).to(InitializerImpl.class).in(Scopes.SINGLETON);
	}

}
