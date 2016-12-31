package io.pantheist.api.schema.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.CreateAction;

final class ListSchemaResponseImpl implements ListSchemaResponse
{
	private final List<ListSchemaItem> childResources;
	private final CreateAction createAction;

	@Inject
	private ListSchemaResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListSchemaItem> childResources,
			@Assisted @JsonProperty("createAction") final CreateAction createAction)
	{
		this.childResources = checkNotNull(childResources);
		this.createAction = checkNotNull(createAction);
	}

	@Override
	public List<ListSchemaItem> childResources()
	{
		return childResources;
	}

	@Override
	public CreateAction createAction()
	{
		return createAction;
	}

}
