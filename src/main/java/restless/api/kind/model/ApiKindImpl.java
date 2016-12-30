package restless.api.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.ListClassifierItem;
import restless.handler.kind.model.JavaClause;
import restless.handler.kind.model.KindLevel;

final class ApiKindImpl implements ApiKind
{
	private final List<? extends ListClassifierItem> childResources;
	private final String kindId;
	private final KindLevel level;
	private final boolean discoverable;
	private final JavaClause java;

	@Inject
	private ApiKindImpl(
			@Nullable @Assisted @JsonProperty("childResources") final List<ListClassifierItem> childResources,
			@Nullable @Assisted("kindId") @JsonProperty("kindId") final String kindId,
			@Assisted @JsonProperty("level") final KindLevel level,
			@Assisted("discoverable") @JsonProperty("discoverable") final boolean discoverable,
			@Nullable @Assisted @JsonProperty("java") final JavaClause java)
	{
		this.childResources = childResources;
		this.kindId = kindId;
		this.level = checkNotNull(level);
		this.discoverable = discoverable;
		this.java = java;
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

}
