package io.pantheist.api.schema.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.DataAction;
import io.pantheist.common.api.model.DeleteAction;
import io.pantheist.common.util.OtherPreconditions;

final class ApiSchemaImpl implements ApiSchema
{
	private final DataAction dataAction;
	private final DeleteAction deleteAction;
	private final String kindUrl;

	@Inject
	private ApiSchemaImpl(
			@Assisted @JsonProperty("dataAction") final DataAction dataAction,
			@Assisted @JsonProperty("deleteAction") final DeleteAction deleteAction,
			@Assisted("kindUrl") @JsonProperty("kindUrl") final String kindUrl)
	{
		this.dataAction = checkNotNull(dataAction);
		this.deleteAction = checkNotNull(deleteAction);
		this.kindUrl = OtherPreconditions.checkNotNullOrEmpty(kindUrl);
	}

	@Override
	public DataAction dataAction()
	{
		return dataAction;
	}

	@Override
	public DeleteAction deleteAction()
	{
		return deleteAction;
	}

	@Override
	public String kindUrl()
	{
		return kindUrl;
	}

}
