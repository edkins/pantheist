package io.pantheist.api.entity.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class ListEntityItemImpl implements ListEntityItem
{
	private final String url;
	private final String entityId;
	private final boolean discovered;

	@Inject
	private ListEntityItemImpl(
			@Assisted("url") @JsonProperty("url") final String url,
			@Assisted("entityId") @JsonProperty("entityId") final String entityId,
			@Assisted("discovered") @JsonProperty("discovered") final boolean discovered)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
		this.entityId = OtherPreconditions.checkNotNullOrEmpty(entityId);
		this.discovered = discovered;
	}

	@Override
	public String entityId()
	{
		return entityId;
	}

	@Override
	public boolean discovered()
	{
		return discovered;
	}

	@Override
	public String url()
	{
		return url;
	}

}
