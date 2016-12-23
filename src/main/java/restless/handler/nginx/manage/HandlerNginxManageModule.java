package restless.handler.nginx.manage;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class HandlerNginxManageModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(NginxService.class);
		bind(NginxService.class).to(NginxServiceImpl.class).in(Scopes.SINGLETON);
	}

}
