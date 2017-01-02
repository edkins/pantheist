package io.pantheist.api.kind.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListEntityItemImpl.class)
public interface ListEntityItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("entityId")
	String entityId();

	@JsonProperty("kindUrl")
	String kindUrl();
}
