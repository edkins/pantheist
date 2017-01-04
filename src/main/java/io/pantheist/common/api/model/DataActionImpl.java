package io.pantheist.common.api.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class DataActionImpl implements DataAction
{
	private final BasicContentType basicType;
	private final String mimeType;
	private final boolean canPut;
	private final String url;

	@Inject
	private DataActionImpl(
			@Assisted @JsonProperty("basicType") final BasicContentType basicType,
			@Assisted("mimeType") @JsonProperty("mimeType") final String mimeType,
			@Assisted("canPut") @JsonProperty("canPut") final boolean canPut,
			@Assisted("url") @JsonProperty("url") final String url)
	{
		this.basicType = checkNotNull(basicType);
		this.mimeType = OtherPreconditions.checkNotNullOrEmpty(mimeType);
		this.canPut = canPut;
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
	}

	@Override
	public BasicContentType basicType()
	{
		return basicType;
	}

	@Override
	public String mimeType()
	{
		return mimeType;
	}

	@Override
	public boolean canPut()
	{
		return canPut;
	}

	@Override
	public String url()
	{
		return url;
	}
}
