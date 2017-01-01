package io.pantheist.api.kind.model;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.Presentation;
import io.pantheist.common.util.OtherPreconditions;

final class ListKindItemImpl implements ListKindItem
{
	private final String url;
	private final String kindUrl;
	private final Presentation instancePresentation;

	@Inject
	private ListKindItemImpl(@Assisted("url") @JsonProperty("url") final String url,
			@Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl,
			@Nullable @Assisted("instancePresentation") @JsonProperty("instancePresentation") final Presentation instancePresentation)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
		this.kindUrl = OtherPreconditions.checkNotNullOrEmpty(kindUrl);
		this.instancePresentation = instancePresentation;
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
	public Presentation instancePresentation()
	{
		return instancePresentation;
	}

}
