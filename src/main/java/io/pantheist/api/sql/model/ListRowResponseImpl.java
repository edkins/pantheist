package io.pantheist.api.sql.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class ListRowResponseImpl implements ListRowResponse
{
	private final List<ListRowItem> childResources;

	@Inject
	private ListRowResponseImpl(@Assisted @JsonProperty("childResources") final List<ListRowItem> childResources)
	{
		this.childResources = checkNotNull(childResources);
	}

	@Override
	public List<ListRowItem> childResources()
	{
		return childResources;
	}

}
