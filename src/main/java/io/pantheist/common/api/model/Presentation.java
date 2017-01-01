package io.pantheist.common.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ApiPresentationImpl.class)
public interface Presentation
{
	@JsonProperty("iconUrl")
	String iconUrl();

	@JsonProperty("openIconUrl")
	String openIconUrl();
}
