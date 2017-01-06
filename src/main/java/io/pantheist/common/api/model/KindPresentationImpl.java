package io.pantheist.common.api.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

final class KindPresentationImpl implements KindPresentation
{
	private final String iconUrl;
	private final String openIconUrl;
	private final String dislayName;
	private final String schemaHint;

	public KindPresentationImpl(
			@Nullable @JsonProperty("iconUrl") final String iconUrl,
			@Nullable @JsonProperty("openIconUrl") final String openIconUrl,
			@Nullable @JsonProperty("displayName") final String dislayName,
			@Nullable @JsonProperty("schemaHint") final String schemaHint)
	{
		this.iconUrl = iconUrl;
		this.openIconUrl = openIconUrl;
		this.dislayName = dislayName;
		this.schemaHint = schemaHint;
	}

	@Override
	public String iconUrl()
	{
		return iconUrl;
	}

	@Override
	public String openIconUrl()
	{
		return openIconUrl;
	}

	@Override
	public String displayName()
	{
		return dislayName;
	}

	@Override
	public String schemaHint()
	{
		return schemaHint;
	}

}
