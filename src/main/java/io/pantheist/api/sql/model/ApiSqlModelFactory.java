package io.pantheist.api.sql.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

public interface ApiSqlModelFactory
{
	ListSqlTableItem listSqlTableItem(
			@Assisted("url") String url,
			@Assisted("name") String name,
			@Assisted("kindUrl") String kindUrl);

	ListSqlTableResponse listSqlTableResponse(List<ListSqlTableItem> childResources);
}
