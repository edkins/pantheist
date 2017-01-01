package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class KindImpl implements Kind
{
	private final String kindId;
	private final KindLevel level;
	private final JavaClause java;
	private final boolean discoverable;
	private final boolean partOfSystem;
	private final int precedence;

	@Inject
	private KindImpl(
			@Assisted("kindId") @JsonProperty("kindId") final String kindId,
			@Assisted @JsonProperty("level") final KindLevel level,
			@Assisted("discoverable") @JsonProperty("discoverable") final Boolean discoverable,
			@Nullable @Assisted @JsonProperty("java") final JavaClause java,
			@Assisted("partOfSystem") @JsonProperty("partOfSystem") final boolean partOfSystem,
			@Assisted("precedence") @JsonProperty("precedence") final int precedence)
	{
		this.kindId = OtherPreconditions.checkNotNullOrEmpty(kindId);
		this.discoverable = checkNotNull(discoverable);
		this.level = checkNotNull(level);
		this.java = java;
		this.partOfSystem = partOfSystem;
		this.precedence = precedence;
	}

	@Override
	public KindLevel level()
	{
		return level;
	}

	@Override
	public JavaClause java()
	{
		return java;
	}

	@Override
	public boolean discoverable()
	{
		return discoverable;
	}

	@Override
	public String kindId()
	{
		return kindId;
	}

	@Override
	public boolean partOfSystem()
	{
		return partOfSystem;
	}

	@Override
	public int precedence()
	{
		return precedence;
	}

}
