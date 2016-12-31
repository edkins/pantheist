package io.pantheist.api.flatdir.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class ListFlatDirResponseImpl implements ListFlatDirResponse
{
	private final List<ListFlatDirItem> childResources;

	@Inject
	private ListFlatDirResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListFlatDirItem> childResources)
	{
		this.childResources = checkNotNull(childResources);
	}

	@Override
	public List<ListFlatDirItem> childResources()
	{
		return childResources;
	}

}
