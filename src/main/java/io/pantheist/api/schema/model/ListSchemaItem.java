package io.pantheist.api.schema.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListSchemaItemImpl.class)
public interface ListSchemaItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("kindUrl")
	String kindUrl();
}
