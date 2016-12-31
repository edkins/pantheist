package io.pantheist.handler.schema.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = SchemaComponentImpl.class)
public interface SchemaComponent
{
	@JsonProperty("isRoot")
	boolean isRoot();

	@JsonProperty("componentId")
	String componentId();
}
