package io.pantheist.handler.kind.model;

import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class KindSchemaImpl implements KindSchema
{
	private final JavaClause java;
	private final Map<String, KindProperty> properties;
	private final ObjectNode identification;

	private KindSchemaImpl(
			@Nullable @JsonProperty("java") final JavaClause java,
			@Nullable @JsonProperty("properties") final Map<String, KindProperty> properties,
			@Nullable @JsonProperty("identification") final ObjectNode identification)
	{
		this.java = java;
		this.properties = properties;
		this.identification = identification;
	}

	@Override
	public JavaClause java()
	{
		return java;
	}

	@Override
	public Map<String, KindProperty> properties()
	{
		return properties;
	}

	@Override
	public ObjectNode identification()
	{
		return identification;
	}

}
