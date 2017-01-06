package io.pantheist.handler.kind.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = AffordanceImpl.class)
public interface Affordance
{
	@JsonProperty("type")
	AffordanceType type();

	@JsonProperty("name")
	String name();

	@JsonProperty("location")
	JsonPath location();

	@JsonProperty("prototypeValue")
	JsonNode prototypeValue();
}
