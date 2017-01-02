package io.pantheist.api.sql.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListSqlTableItemImpl.class)
public interface ListSqlTableItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("name")
	String name();

	@JsonProperty("kindUrl")
	String kindUrl();
}
