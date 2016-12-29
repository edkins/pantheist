package restless.api.kind.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class ListEntityItemImpl implements ListEntityItem
{
	private final String entityId;
	private final boolean discovered;

	@Inject
	private ListEntityItemImpl(
			@Assisted("entityId") @JsonProperty("entityId") final String entityId,
			@Assisted("discovered") @JsonProperty("discovered") final boolean discovered)
	{
		this.entityId = OtherPreconditions.checkNotNullOrEmpty(entityId);
		this.discovered = discovered;
	}

	@Override
	public String entityId()
	{
		return entityId;
	}

	@Override
	public boolean discovered()
	{
		return discovered;
	}

}
