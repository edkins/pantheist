package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

final class KindSchemaImpl implements KindSchema
{
	private final JavaClause java;
	private final List<String> subKindOf;

	@Inject
	private KindSchemaImpl(
			@Nullable @Assisted @JsonProperty("java") final JavaClause java,
			@Assisted("subKindOf") @JsonProperty("subKindOf") final List<String> subKindOf)
	{
		this.java = java;
		this.subKindOf = checkNotNull(subKindOf);
	}

	@Override
	public JavaClause java()
	{
		return java;
	}

	@Override
	public List<String> subKindOf()
	{
		return subKindOf;
	}

}
