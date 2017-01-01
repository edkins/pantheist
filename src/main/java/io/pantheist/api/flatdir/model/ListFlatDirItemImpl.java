package io.pantheist.api.flatdir.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

public class ListFlatDirItemImpl implements ListFlatDirItem
{
	private final String url;
	private final String relativePath;
	private final String kindUrl;

	@Inject
	private ListFlatDirItemImpl(
			@Assisted("url") @JsonProperty("url") final String url,
			@Assisted("relativePath") @JsonProperty("relativePath") final String relativePath,
			@Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
		this.relativePath = OtherPreconditions.checkNotNullOrEmpty(relativePath);
		this.kindUrl = OtherPreconditions.checkNotNullOrEmpty(kindUrl);
	}

	@Override
	public String url()
	{
		return url;
	}

	@Override
	public String relativePath()
	{
		return relativePath;
	}

	@Override
	public String kindUrl()
	{
		return kindUrl;
	}

}
