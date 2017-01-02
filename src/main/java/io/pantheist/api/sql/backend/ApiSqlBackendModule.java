package io.pantheist.api.sql.backend;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public final class ApiSqlBackendModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(SqlBackend.class);
		bind(SqlBackend.class).to(SqlBackendImpl.class).in(Scopes.SINGLETON);
	}

}
