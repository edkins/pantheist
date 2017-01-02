package io.pantheist.api.sql.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListRowItemImpl.class)
public interface ListRowItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("kindUrl")
	String kindUrl();
}
