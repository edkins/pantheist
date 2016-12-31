package io.pantheist.api.kind.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListKindItemImpl.class)
public interface ListKindItem
{
	@JsonProperty("url")
	String url();
}
