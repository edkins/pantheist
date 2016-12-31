package io.pantheist.api.management.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class ListConfigItemImpl implements ListConfigItem
{
	private final String url;

	@Inject
	private ListConfigItemImpl(@Assisted("url") @JsonProperty("url") final String url)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
	}

	@Override
	public String url()
	{
		return url;
	}

}
