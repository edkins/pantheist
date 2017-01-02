package io.pantheist.common.shared.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * These are serialized as part of Kind and ApiKind.
 */
@JsonDeserialize(as = GenericPropertyImpl.class)
public interface GenericProperty
{
	@JsonProperty("name")
	String name();

	@JsonProperty("type")
	PropertyType type();

	@JsonInclude(Include.NON_DEFAULT)
	@JsonProperty("isIdentifier")
	boolean isIdentifier();
}
