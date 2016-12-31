package io.pantheist.api.flatdir.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class ListFileItemImpl implements ListFileItem
{
	private final String url;
	private final String fileName;

	@Inject
	private ListFileItemImpl(
			@Assisted("url") @JsonProperty("url") final String url,
			@Assisted("fileName") @JsonProperty("fileName") final String fileName)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
		this.fileName = OtherPreconditions.checkNotNullOrEmpty(fileName);
	}

	@Override
	public String url()
	{
		return url;
	}

	@Override
	public String fileName()
	{
		return fileName;
	}

}
