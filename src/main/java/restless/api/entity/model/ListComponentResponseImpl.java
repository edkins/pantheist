package restless.api.entity.model;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

final class ListComponentResponseImpl implements ListComponentResponse
{
	private final List<ListComponentItem> childResources;

	@Inject
	private ListComponentResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListComponentItem> childResources)
	{
		this.childResources = ImmutableList.copyOf(childResources);
	}

	@Override
	public List<ListComponentItem> childResources()
	{
		return childResources;
	}

}
