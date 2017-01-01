package io.pantheist.api.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.api.model.Presentation;

@JsonDeserialize(as = ListKindItemImpl.class)
public interface ListKindItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("kindUrl")
	String kindUrl();

	@Nullable
	@JsonProperty("instancePresentation")
	Presentation instancePresentation();
}
