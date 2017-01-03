package io.pantheist.handler.kind.model;

import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.common.shared.model.CommonSharedModelFactory;
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
	 * For object-array types only. Specifies the properties supported
	 * by each element of the array.
	 */
	@Nullable
	@JsonProperty("itemProperties")
	Map<String, TypeInfo> itemProperties();

	@JsonIgnore
	TypeInfo typeInfo(CommonSharedModelFactory modelFactory);
}
