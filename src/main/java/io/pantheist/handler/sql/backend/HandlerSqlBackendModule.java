package io.pantheist.handler.sql.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public final class HandlerSqlBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(SqlService.class);
		bind(SqlService.class).to(SqlServiceImpl.class).in(Scopes.SINGLETON);
		bind(SqlCoreService.class).to(SqlCoreServiceImpl.class).in(Scopes.SINGLETON);
	}

}
