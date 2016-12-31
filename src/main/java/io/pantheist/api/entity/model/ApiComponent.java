package io.pantheist.api.entity.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.handler.java.model.JavaComponent;
import io.pantheist.handler.schema.model.SchemaComponent;

@JsonDeserialize(as = ApiComponentImpl.class)
public interface ApiComponent
{
	@Nullable
	@JsonProperty("jsonSchema")
	SchemaComponent jsonSchema();

	@Nullable
	@JsonProperty("java")
	JavaComponent java();
}
