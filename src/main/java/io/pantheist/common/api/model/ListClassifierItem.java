package io.pantheist.common.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ListClassifierItemImpl.class)
public interface ListClassifierItem
{
	@JsonProperty("url")
	String url();

	@JsonProperty("classifierSegment")
	String classifierSegment();

	/**
	 * A hint to the UI to hide this segment in the tree view.
	 *
	 * Otherwise e.g. the java package view has one extra level from what you'd expect.
	 */
	@JsonProperty("suggestHiding")
	boolean suggestHiding();
}
