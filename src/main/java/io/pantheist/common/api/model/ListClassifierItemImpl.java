package io.pantheist.common.api.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class ListClassifierItemImpl implements ListClassifierItem
{
	private final String url;
	private final String classifierSegment;
	private final boolean suggestHiding;
	private final String kindUrl;

	@Inject
	private ListClassifierItemImpl(
			@Assisted("url") @JsonProperty("url") final String url,
			@Assisted("classifierSegment") @JsonProperty("classifierSegment") final String classifierSegment,
			@Assisted("suggestHiding") @JsonProperty("suggestHiding") final boolean suggestHiding,
			@Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
		this.classifierSegment = OtherPreconditions.checkNotNullOrEmpty(classifierSegment);
		this.suggestHiding = suggestHiding;
		this.kindUrl = OtherPreconditions.checkNotNullOrEmpty(kindUrl);
	}

	@Override
	public String url()
	{
		return url;
	}

	@Override
	public String classifierSegment()
	{
		return classifierSegment;
	}

	@Override
	public boolean suggestHiding()
	{
		return suggestHiding;
	}

	@Override
	public String kindUrl()
	{
		return kindUrl;
	}

}
