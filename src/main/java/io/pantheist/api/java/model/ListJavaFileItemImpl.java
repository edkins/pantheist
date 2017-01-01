package io.pantheist.api.java.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class ListJavaFileItemImpl implements ListJavaFileItem
{
	private final String url;
	private final String kindUrl;

	@Inject
	private ListJavaFileItemImpl(
			@Assisted("url") @JsonProperty("url") final String url,
			@Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
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

}
