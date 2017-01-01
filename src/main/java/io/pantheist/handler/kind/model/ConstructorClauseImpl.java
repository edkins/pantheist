package io.pantheist.handler.kind.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

final class ConstructorClauseImpl implements ConstructorClause
{
	private final ArgClause anyArg;

	private ConstructorClauseImpl(@Nullable @JsonProperty("anyArg") final ArgClause anyArg)
	{
		this.anyArg = anyArg;
	}

	@Override
	public ArgClause anyArg()
	{
		return anyArg;
	}

}
