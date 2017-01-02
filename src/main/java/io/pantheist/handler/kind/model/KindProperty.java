package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.shared.model.PropertyType;
import io.pantheist.common.shared.model.TypeInfo;

@JsonDeserialize(as = KindPropertyImpl.class)
public interface KindProperty
{
	@JsonProperty("type")
	PropertyType type();

	@JsonInclude(Include.NON_DEFAULT)
	@JsonProperty("isIdentifier")
	boolean isIdentifier();

	/**
	 * For array types only. Specifies the type of an individual element.
	 * (note "isIdentifier" doesn't make sense for array elements)
	 */
	@Nullable
	@JsonProperty("items")
	TypeInfo items();
}
