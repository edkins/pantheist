package restless.common.http;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class CommonHttpModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(Resp.class);
		bind(Resp.class).to(RespImpl.class).in(Scopes.SINGLETON);
	}

}
