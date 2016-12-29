package restless.api.management.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class ListComponentItemImpl implements ListComponentItem
{
	private final String componentId;

	@Inject
	private ListComponentItemImpl(@Assisted("componentId") @JsonProperty("componentId") final String componentId)
	{
		this.componentId = OtherPreconditions.checkNotNullOrEmpty(componentId);
	}

	@Override
	public String componentId()
	{
		return componentId;
	}

}
