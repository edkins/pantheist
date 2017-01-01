package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class JavaClauseImpl implements JavaClause
{
	private final boolean required;
	private final JavaKind javaKind;
	private final AnnotationClause anyAnnotation;
	private final ConstructorClause anyConstructor;

	private JavaClauseImpl(
			@JsonProperty("required") final boolean required,
			@Nullable @Assisted @JsonProperty("javaKind") final JavaKind javaKind,
			@Nullable @JsonProperty("anyAnnotation") final AnnotationClause anyAnnotation,
			@JsonProperty("anyConstructor") final ConstructorClause anyConstructor)
	{
		this.required = required;
		this.javaKind = javaKind;
		this.anyAnnotation = anyAnnotation;
		this.anyConstructor = anyConstructor;
	}

	@Override
	public boolean required()
	{
		return required;
	}

	@Override
	public JavaKind javaKind()
	{
		return javaKind;
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
