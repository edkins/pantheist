package io.pantheist.common.shared.model;

import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = TypeInfoImpl.class)
public interface TypeInfo
{
	@JsonProperty("type")
	PropertyType type();

	/**
	 * For array
	 */
	@Nullable
	@JsonProperty("items")
	TypeInfo items();

	/**
	 * For object
	 */
	@Nullable
	@JsonProperty("properties")
	Map<String, TypeInfo> properties();
}
