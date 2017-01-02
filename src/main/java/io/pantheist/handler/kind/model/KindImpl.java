package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.Presentation;
import io.pantheist.common.util.OtherPreconditions;

final class KindImpl implements Kind
{
	private final String kindId;
	private final boolean partOfSystem;
	private final KindSchema schema;
	private final Presentation instancePresentation;

	@Inject
	private KindImpl(
			@Assisted("kindId") @JsonProperty("kindId") final String kindId,
			@Assisted("partOfSystem") @JsonProperty("partOfSystem") final boolean partOfSystem,
			@Nullable @Assisted("instancePresentation") @JsonProperty("instancePresentation") final Presentation instancePresentation,
			@Assisted @JsonProperty("schema") final KindSchema schema)
	{
		this.kindId = OtherPreconditions.checkNotNullOrEmpty(kindId);
		this.partOfSystem = partOfSystem;
		this.schema = checkNotNull(schema);
		this.instancePresentation = instancePresentation;
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
	public Presentation instancePresentation()
	{
		return instancePresentation;
	}

	@Override
	public KindSchema schema()
	{
		return schema;
	}

	@Override
	public String toString()
	{
		return "[kind " + kindId + "]";
	}
}
