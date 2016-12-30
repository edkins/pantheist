package restless.api.management.model;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

final class ListJavaPkgResponseImpl implements ListJavaPkgResponse
{
	private final List<ListJavaPkgItem> childResources;

	@Inject
	private ListJavaPkgResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListJavaPkgItem> childResources)
	{
		this.childResources = ImmutableList.copyOf(childResources);
	}

	@Override
	public List<ListJavaPkgItem> childResources()
	{
		return childResources;
	}

}
