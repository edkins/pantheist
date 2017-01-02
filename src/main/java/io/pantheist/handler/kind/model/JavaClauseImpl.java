package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

final class JavaClauseImpl implements JavaClause
{
	private final AnnotationClause anyAnnotation;
	private final ConstructorClause anyConstructor;

	private JavaClauseImpl(
			@Nullable @JsonProperty("anyAnnotation") final AnnotationClause anyAnnotation,
			@JsonProperty("anyConstructor") final ConstructorClause anyConstructor)
	{
		this.anyAnnotation = anyAnnotation;
		this.anyConstructor = anyConstructor;
	}

	@Override
	public AnnotationClause anyAnnotation()
	{
		return anyAnnotation;
	}

	@Override
	public ConstructorClause anyConstructor()
	{
		return anyConstructor;
	}

}
