package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

final class ArgClauseImpl implements ArgClause
{
	private final AnnotationClause anyAnnotation;

	private ArgClauseImpl(@Nullable @JsonProperty("anyAnnotation") final AnnotationClause anyAnnotation)
	{
		this.anyAnnotation = anyAnnotation;
	}

	@Override
	public AnnotationClause anyAnnotation()
	{
		return anyAnnotation;
	}

}
