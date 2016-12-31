package io.pantheist.common.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = AdditionalStructureItemImpl.class)
public interface AdditionalStructureItem
{
	/**
	 * Whether this path segment is literal.
	 *
	 * If false, it's assumed to be a variable that matches one path segment.
	 */
	@JsonProperty("literal")
	boolean literal();

	/**
	 * The literal name of this path segment, or else the name of the associated variable.
	 */
	@JsonProperty("name")
	String name();

	/**
	 * A hint to the UI to hide this segment
	 */
	@JsonProperty("suggestHiding")
	boolean suggestHiding();
}
