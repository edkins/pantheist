package io.pantheist.api.entity.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class ListComponentItemImpl implements ListComponentItem
{
	private final String url;
	private final String componentId;

	@Inject
	private ListComponentItemImpl(
			@Assisted("url") @JsonProperty("url") final String url,
			@Assisted("componentId") @JsonProperty("componentId") final String componentId)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
		this.componentId = OtherPreconditions.checkNotNullOrEmpty(componentId);
	}

	@Override
	public String componentId()
	{
		return componentId;
	}

	@Override
	public String url()
	{
		return url;
	}

}
