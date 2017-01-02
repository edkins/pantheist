package io.pantheist.handler.kind.model;

import com.fasterxml.jackson.annotation.JsonProperty;

final class JavaClauseImpl implements JavaClause
{
	private final ConstructorClause anyConstructor;

	private JavaClauseImpl(
			@JsonProperty("anyConstructor") final ConstructorClause anyConstructor)
	{
		this.anyConstructor = anyConstructor;
	}

	@Override
	public ConstructorClause anyConstructor()
	{
		return anyConstructor;
	}

}
