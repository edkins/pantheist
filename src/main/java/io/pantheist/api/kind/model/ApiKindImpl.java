package io.pantheist.api.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.KindPresentation;
import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.common.api.model.ReplaceAction;
import io.pantheist.handler.kind.model.KindSchema;

final class ApiKindImpl implements ApiKind
{
	private final List<? extends ListClassifierItem> childResources;
	private final String kindId;
	private final KindSchema schema;
	private final boolean partOfSystem;
	private final ReplaceAction replaceAction;
	private final KindPresentation presentation;
	private final CreateAction createAction;
	private final String displayName;
	private final String url;
	private final String kindUrl;

	@Inject
	private ApiKindImpl(
			@Nullable @Assisted("url") @JsonProperty("url") final String url,
			@Nullable @Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl,
			@Nullable @Assisted @JsonProperty("childResources") final List<ListClassifierItem> childResources,
			@Nullable @Assisted @JsonProperty("replaceAction") final ReplaceAction replaceAction,
			@Nullable @Assisted("kindId") @JsonProperty("kindId") final String kindId,
			@Assisted @JsonProperty("schema") final KindSchema schema,
			@Assisted("partOfSystem") @JsonProperty("partOfSystem") final boolean partOfSystem,
			@Nullable @Assisted @JsonProperty("presentation") final KindPresentation presentation,
			@Nullable @Assisted @JsonProperty("createAction") final CreateAction createAction,
			@Nullable @Assisted("displayName") @JsonProperty("displayName") final String displayName)
	{
		this.url = url;
		this.kindUrl = kindUrl;
		this.childResources = childResources;
		this.replaceAction = replaceAction;
		this.kindId = kindId;
		this.partOfSystem = partOfSystem;
		this.presentation = presentation;
		this.schema = checkNotNull(schema);
		this.createAction = createAction;
		this.displayName = displayName;
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
	public KindPresentation presentation()
	{
		return presentation;
	}

	@Override
	public KindSchema schema()
	{
		return schema;
	}

	@Override
	public CreateAction createAction()
	{
		return createAction;
	}

	@Override
	public String displayName()
	{
		return displayName;
	}

	@Override
	public String url()
	{
		return url;
	}

	@Override
	public String kindUrl()
	{
		return kindUrl;
	}
}
