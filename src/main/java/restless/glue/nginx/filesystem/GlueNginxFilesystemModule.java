package restless.glue.nginx.filesystem;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class GlueNginxFilesystemModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(NginxFilesystemGlue.class);
		bind(NginxFilesystemGlue.class).to(NginxFilesystemGlueImpl.class).in(Scopes.SINGLETON);
	}

}
