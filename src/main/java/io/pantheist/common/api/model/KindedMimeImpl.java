package io.pantheist.common.api.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class KindedMimeImpl implements KindedMime
{
	private final String kindUrl;
	private final String mimeType;
	private final String text;

	@Inject
	private KindedMimeImpl(
			@Assisted("kindUrl") final String kindUrl,
			@Assisted("mimeType") final String mimeType,
			@Assisted("text") final String text)
	{
		this.kindUrl = OtherPreconditions.checkNotNullOrEmpty(kindUrl);
		this.mimeType = OtherPreconditions.checkNotNullOrEmpty(mimeType);
		this.text = checkNotNull(text);
	}

	@Override
	public String kindUrl()
	{
		return kindUrl;
	}

	@Override
	public String mimeType()
	{
		return mimeType;
	}

	@Override
	public String text()
	{
		return text;
	}

}
