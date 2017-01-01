package io.pantheist.api.java.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListJavaPkgItemImpl.class)
public interface ListJavaPkgItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("name")
	String name();

	@JsonProperty("kindUrl")
	String kindUrl();
}
