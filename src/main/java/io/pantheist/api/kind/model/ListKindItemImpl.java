package io.pantheist.api.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class ListKindItemImpl implements ListKindItem
{
	private final String url;
	private final String name;
	private final String kindUrl;

	@Inject
	private ListKindItemImpl(
			@Assisted("url") @JsonProperty("url") final String url,
			@Assisted("name") @JsonProperty("name") final String name,
			@Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.kindUrl = checkNotNull(kindUrl);
	}

	@Override
	public String url()
	{
		return url;
	}

	@Override
	public String kindUrl()
	{
		return kindUrl;
	}

	@Override
	public String name()
	{
		return name;
	}

}
