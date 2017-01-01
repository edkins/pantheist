package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

final class AnnotationClauseImpl implements AnnotationClause
{
	private final String name;

	private AnnotationClauseImpl(@Nullable @JsonProperty("name") final String name)
	{
		this.name = name;
	}

	@Override
	public String name()
	{
		return name;
	}

}
