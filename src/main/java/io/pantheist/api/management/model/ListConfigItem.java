package io.pantheist.api.management.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListConfigItemImpl.class)
public interface ListConfigItem
{
	@JsonProperty("url")
	String url();
}
