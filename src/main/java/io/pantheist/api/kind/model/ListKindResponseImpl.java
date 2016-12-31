package io.pantheist.api.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class ListKindResponseImpl implements ListKindResponse
{
	private final List<ListKindItem> childResources;

	@Inject
	private ListKindResponseImpl(@Assisted @JsonProperty("childResources") final List<ListKindItem> childResources)
	{
		this.childResources = checkNotNull(childResources);
	}

	@Override
	public List<ListKindItem> childResources()
	{
		return childResources;
	}

}
