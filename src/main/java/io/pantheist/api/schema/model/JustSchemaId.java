package io.pantheist.api.schema.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Retrieve the schema id from a json schema. Ignore the other stuff.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(as = JustSchemaIdImpl.class)
public interface JustSchemaId
{
	/**
	 * Having null here is technically valid, but will be rejected by the POST operation.
	 */
	@Nullable
	@JsonProperty("id")
	String id();
}
