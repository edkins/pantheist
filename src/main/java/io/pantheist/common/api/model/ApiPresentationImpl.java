package io.pantheist.common.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

final class ApiPresentationImpl implements Presentation
{
	private final String iconUrl;
	private final String openIconUrl;

	public ApiPresentationImpl(
			@JsonProperty("iconUrl") final String iconUrl,
			@JsonProperty("openIconUrl") final String openIconUrl)
	{
		this.iconUrl = iconUrl;
		this.openIconUrl = openIconUrl;
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

}
