package io.pantheist.api.flatdir.model;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

final class ListFileResponseImpl implements ListFileResponse
{
	private final List<ListFileItem> childResources;

	@Inject
	private ListFileResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListFileItem> childResources)
	{
		this.childResources = ImmutableList.copyOf(childResources);
	}

	@Override
	public List<ListFileItem> childResources()
	{
		return childResources;
	}

}
