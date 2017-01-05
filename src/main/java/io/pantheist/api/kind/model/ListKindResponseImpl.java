package io.pantheist.api.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.CreateAction;

final class ListKindResponseImpl implements ListKindResponse
{
	private final List<ListKindItem> childResources;
	private final CreateAction createAction;

	@Inject
	private ListKindResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListKindItem> childResources,
			@Assisted @JsonProperty("createAction") final CreateAction createAction)
	{
		this.childResources = checkNotNull(childResources);
		this.createAction = checkNotNull(createAction);
	}

	@Override
	public List<ListKindItem> childResources()
	{
		return childResources;
	}

	@Override
	public CreateAction createAction()
	{
		return createAction;
	}

}
