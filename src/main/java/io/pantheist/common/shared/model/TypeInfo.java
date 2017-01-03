package io.pantheist.common.shared.model;

import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = TypeInfoImpl.class)
public interface TypeInfo
{
	@JsonProperty("type")
	PropertyType type();

	/**
	 * For type object-array
	 */
	@Nullable
	@JsonProperty("itemProperties")
	Map<String, TypeInfo> itemProperties();
}
