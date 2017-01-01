package io.pantheist.common.api.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class ReplaceActionImpl implements ReplaceAction
{
	private final BasicContentType basicType;
	private final String mimeType;

	@Inject
	private ReplaceActionImpl(
			@Assisted @JsonProperty("basicType") final BasicContentType basicType,
			@Assisted("mimeType") @JsonProperty("mimeType") final String mimeType)
	{
		this.basicType = checkNotNull(basicType);
		this.mimeType = OtherPreconditions.checkNotNullOrEmpty(mimeType);
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
}
