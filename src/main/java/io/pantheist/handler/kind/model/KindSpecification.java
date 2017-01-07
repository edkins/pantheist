package io.pantheist.handler.kind.model;

import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = KindSpecificationImpl.class)
public interface KindSpecification
{
	@Nullable
	@JsonProperty("jsonSchema")
	JsonNode jsonSchema();

	@Nullable
	@JsonProperty("mimeType")
	String mimeType();

	@Nullable
	@JsonProperty("hooks")
	List<KindHook> hooks();
}
