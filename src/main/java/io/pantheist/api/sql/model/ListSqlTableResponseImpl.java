package io.pantheist.api.sql.model;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

final class ListSqlTableResponseImpl implements ListSqlTableResponse
{
	private final List<ListSqlTableItem> childResources;

	@Inject
	private ListSqlTableResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListSqlTableItem> childResources)
	{
		this.childResources = ImmutableList.copyOf(childResources);
	}

	@Override
	public List<ListSqlTableItem> childResources()
	{
		return childResources;
	}
}
