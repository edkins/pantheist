package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = JavaClauseImpl.class)
public interface JavaClause
{
	@JsonProperty("required")
	boolean required();

	@Nullable
	@JsonProperty("javaKind")
	JavaKind javaKind();

	@Nullable
	@JsonProperty("anyAnnotation")
	AnnotationClause anyAnnotation();
}
