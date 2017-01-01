package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ArgClauseImpl.class)
public interface ArgClause
{
	@Nullable
	@JsonProperty("anyAnnotation")
	AnnotationClause anyAnnotation();
}
