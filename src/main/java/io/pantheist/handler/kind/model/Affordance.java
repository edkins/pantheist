package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = AffordanceImpl.class)
public interface Affordance
{
	@JsonProperty("type")
	AffordanceType type();

	@Nullable
	@JsonProperty("name")
	String name();

	@Nullable
	@JsonProperty("location")
	SerializableJsonPointer location();

	@Nullable
	@JsonProperty("prototypeValue")
	JsonNode prototypeValue();
}
