package io.pantheist.api.sql.model;

import java.util.List;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.DataAction;

public interface ApiSqlModelFactory
{
	ListSqlTableItem listSqlTableItem(
			@Assisted("url") String url,
			@Assisted("name") String name,
			@Assisted("kindUrl") String kindUrl);

	ListSqlTableResponse listSqlTableResponse(List<ListSqlTableItem> childResources);

	ListRowItem listRowItem(
			@Assisted("url") String url,
			@Assisted("kindUrl") String kindUrl);

	ListRowResponse listRowResponse(
			List<ListRowItem> childResources);

	ApiSqlRow sqlRow(DataAction dataAction);
}
