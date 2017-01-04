package io.pantheist.common.api.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = ApiPresentationImpl.class)
public interface KindPresentation
{
	@Nullable
	@JsonProperty("iconUrl")
	String iconUrl();

	@Nullable
	@JsonProperty("openIconUrl")
	String openIconUrl();

	/**
	 * You need to display something, but if this is null the kindId will be used instead.
	 */
	@Nullable
	@JsonProperty("displayName")
	String displayName();
}
