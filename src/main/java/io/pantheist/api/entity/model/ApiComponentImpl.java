package io.pantheist.api.entity.model;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.handler.java.model.JavaComponent;
import io.pantheist.handler.schema.model.SchemaComponent;

final class ApiComponentImpl implements ApiComponent
{
	private final SchemaComponent jsonSchema;
	private final JavaComponent java;

	@Inject
	public ApiComponentImpl(
			@Nullable @Assisted @JsonProperty("jsonSchema") final SchemaComponent jsonSchema,
			@Nullable @Assisted @JsonProperty("java") final JavaComponent java)
	{
		this.jsonSchema = jsonSchema;
		this.java = java;
	}

	@Override
	public SchemaComponent jsonSchema()
	{
		return jsonSchema;
	}

	@Override
	public JavaComponent java()
	{
		return java;
	}

}
