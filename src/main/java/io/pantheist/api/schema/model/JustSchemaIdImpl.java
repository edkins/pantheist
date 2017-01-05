package io.pantheist.api.schema.model;

import com.fasterxml.jackson.annotation.JsonProperty;

final class JustSchemaIdImpl implements JustSchemaId
{
	private final String id;

	private JustSchemaIdImpl(@JsonProperty("id") final String id)
	{
		this.id = id;
	}

	@Override
	public String id()
	{
		return id;
	}

}
