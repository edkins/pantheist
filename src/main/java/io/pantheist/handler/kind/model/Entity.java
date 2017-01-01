package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.pantheist.handler.java.model.JavaFileId;

@JsonDeserialize(as = EntityImpl.class)
public interface Entity
{
	@JsonProperty("entityId")
	String entityId();

	@JsonProperty("discovered")
	boolean discovered();

	@JsonProperty("kindId")
	String kindId();

	@Nullable
	@JsonProperty("jsonSchemaId")
	String jsonSchemaId();

	@Nullable
	@JsonProperty("javaFileId")
	JavaFileId javaFileId();
}
