package io.pantheist.api.management.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class ListConfigResponseImpl implements ListConfigResponse
{
	private final List<ListConfigItem> childResources;

	@Inject
	private ListConfigResponseImpl(@Assisted @JsonProperty("childResources") final List<ListConfigItem> childResources)
	{
		this.childResources = checkNotNull(childResources);
	}

	@Override
	public List<ListConfigItem> childResources()
	{
		return childResources;
	}

}
