package io.pantheist.api.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.common.api.model.ReplaceAction;
import io.pantheist.handler.kind.model.JavaClause;
import io.pantheist.handler.kind.model.KindLevel;

final class ApiKindImpl implements ApiKind
{
	private final List<? extends ListClassifierItem> childResources;
	private final String kindId;
	private final KindLevel level;
	private final boolean discoverable;
	private final JavaClause java;
	private final boolean partOfSystem;
	private final ReplaceAction replaceAction;
	private final List<String> subKindOf;

	@Inject
	private ApiKindImpl(
			@Nullable @Assisted @JsonProperty("childResources") final List<ListClassifierItem> childResources,
			@Nullable @Assisted @JsonProperty("replaceAction") final ReplaceAction replaceAction,
			@Nullable @Assisted("kindId") @JsonProperty("kindId") final String kindId,
			@Assisted @JsonProperty("level") final KindLevel level,
			@Assisted("discoverable") @JsonProperty("discoverable") final boolean discoverable,
			@Nullable @Assisted @JsonProperty("java") final JavaClause java,
			@Assisted("partOfSystem") @JsonProperty("partOfSystem") final boolean partOfSystem,
			@Assisted("subKindOf") @JsonProperty("subKindOf") final List<String> subKindOf)
	{
		this.childResources = childResources;
		this.replaceAction = replaceAction;
		this.kindId = kindId;
		this.level = checkNotNull(level);
		this.discoverable = discoverable;
		this.java = java;
		this.partOfSystem = partOfSystem;
		this.subKindOf = ImmutableList.copyOf(subKindOf);
	}

	@Override
	public List<? extends ListClassifierItem> childResources()
	{
		return childResources;
	}

	@Override
	public String kindId()
	{
		return kindId;
	}

	@Override
	public KindLevel level()
	{
		return level;
	}

	@Override
	public boolean discoverable()
	{
		return discoverable;
	}

	@Override
	public JavaClause java()
	{
		return java;
	}

	@Override
	public boolean partOfSystem()
	{
		return partOfSystem;
	}

	@Override
	public ReplaceAction replaceAction()
	{
		return replaceAction;
	}

	@Override
	public List<String> subKindOf()
	{
		return subKindOf;
	}
}
