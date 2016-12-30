package restless.api.entity.model;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

final class ListEntityResponseImpl implements ListEntityResponse
{
	private final List<ListEntityItem> childResources;

	@Inject
	private ListEntityResponseImpl(@Assisted @JsonProperty("childResources") final List<ListEntityItem> childResources)
	{
		this.childResources = ImmutableList.copyOf(childResources);
	}

	@Override
	public List<ListEntityItem> childResources()
	{
		return childResources;
	}

}
