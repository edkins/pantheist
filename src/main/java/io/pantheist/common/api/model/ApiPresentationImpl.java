package io.pantheist.common.api.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

final class ApiPresentationImpl implements KindPresentation
{
	private final String iconUrl;
	private final String openIconUrl;
	private final String dislayName;

	public ApiPresentationImpl(
			@Nullable @JsonProperty("iconUrl") final String iconUrl,
			@Nullable @JsonProperty("openIconUrl") final String openIconUrl,
			@Nullable @JsonProperty("displayName") final String dislayName)
	{
		this.iconUrl = iconUrl;
		this.openIconUrl = openIconUrl;
		this.dislayName = dislayName;
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

}
