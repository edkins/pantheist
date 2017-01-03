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

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = KindPropertyImpl.class)
public interface KindProperty
{
	@JsonProperty("type")
	PropertyType type();

	@JsonInclude(Include.NON_DEFAULT)
	@JsonProperty("isIdentifier")
	boolean isIdentifier();

	/**
	 * For array types only. Specifies the schema for
	 * each element in the array.
	 */
	@Nullable
	@JsonProperty("items")
	TypeInfo items();

	/**
	 * For array types only. Specifies the schema for
	 * each particular property of the object.
	 */
	@Nullable
	@JsonProperty("properties")
	Map<String, TypeInfo> properties();

	@JsonIgnore
	TypeInfo typeInfo(CommonSharedModelFactory modelFactory);
}
