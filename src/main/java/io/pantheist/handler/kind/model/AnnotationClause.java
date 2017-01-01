package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = AnnotationClauseImpl.class)
public interface AnnotationClause
{
	@Nullable
	String name();
}
