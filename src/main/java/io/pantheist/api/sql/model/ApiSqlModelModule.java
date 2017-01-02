package io.pantheist.api.sql.model;

import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ApiSqlModelModule extends PrivateModule
{

	@Override
	protected void configure()
	{
		expose(ApiSqlModelFactory.class);
		install(new FactoryModuleBuilder()
				.implement(ListSqlTableItem.class, ListSqlTableItemImpl.class)
				.implement(ListSqlTableResponse.class, ListSqlTableResponseImpl.class)
				.build(ApiSqlModelFactory.class));
	}

}
