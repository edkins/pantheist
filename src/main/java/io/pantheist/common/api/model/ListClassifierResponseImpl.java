package io.pantheist.common.api.model;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

final class ListClassifierResponseImpl implements ListClassifierResponse
{
	private final List<ListClassifierItem> childResources;

	@Inject
	private ListClassifierResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListClassifierItem> childResources)
	{
		this.childResources = ImmutableList.copyOf(childResources);
	}

	@Override
	public List<ListClassifierItem> childResources()
	{
		return childResources;
	}
}
