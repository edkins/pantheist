package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ConstructorClauseImpl.class)
public interface ConstructorClause
{
	@Nullable
	@JsonProperty("anyArg")
	ArgClause anyArg();
}
