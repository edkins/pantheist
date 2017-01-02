package io.pantheist.handler.kind.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.shared.model.PropertyType;

@JsonDeserialize(as = KindPropertyImpl.class)
public interface KindProperty
{
	@JsonProperty("type")
	PropertyType type();

	@JsonInclude(Include.NON_DEFAULT)
	@JsonProperty("isIdentifier")
	boolean isIdentifier();
}
