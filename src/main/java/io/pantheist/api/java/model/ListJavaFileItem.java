package io.pantheist.api.java.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListJavaFileItemImpl.class)
public interface ListJavaFileItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("kind")
	String kind();
}
