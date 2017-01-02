package io.pantheist.handler.sql.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public final class HandlerSqlModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(SqlModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(SqlProperty.class, SqlPropertyImpl.class)
				.build(SqlModelFactory.class));
	}

}
