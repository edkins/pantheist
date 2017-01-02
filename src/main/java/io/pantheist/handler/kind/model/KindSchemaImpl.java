package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.pantheist.common.shared.model.GenericProperty;

final class KindSchemaImpl implements KindSchema
{
	private final JavaClause java;
	private final List<String> subKindOf;
	private final List<GenericProperty> properties;

	private KindSchemaImpl(
			@Nullable @JsonProperty("java") final JavaClause java,
			@JsonProperty("subKindOf") final List<String> subKindOf,
			@Nullable @JsonProperty("properties") final List<GenericProperty> properties)
	{
		this.java = java;
		this.subKindOf = checkNotNull(subKindOf);
		this.properties = properties;
	}

	@Override
	public JavaClause java()
	{
		return java;
	}

	@Override
	public List<String> subKindOf()
	{
		return subKindOf;
	}

	@Override
	public List<GenericProperty> properties()
	{
		return properties;
	}

}
