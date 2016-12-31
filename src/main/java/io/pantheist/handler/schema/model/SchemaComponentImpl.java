package io.pantheist.handler.schema.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class SchemaComponentImpl implements SchemaComponent
{
	private final boolean isRoot;
	private final String componentId;

	@Inject
	public SchemaComponentImpl(@Assisted("isRoot") @JsonProperty("isRoot") final boolean isRoot,
			@Assisted("componentId") @JsonProperty("componentId") final String componentId)
	{
		this.isRoot = isRoot;
		this.componentId = OtherPreconditions.checkNotNullOrEmpty(componentId);
	}

	@Override
	public boolean isRoot()
	{
		return isRoot;
	}

	@Override
	public String componentId()
	{
		return componentId;
	}

}
