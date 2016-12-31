package restless.api.java.model;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

final class ListJavaFileResponseImpl implements ListJavaFileResponse
{
	private final List<ListJavaFileItem> childResources;

	@Inject
	private ListJavaFileResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListJavaFileItem> childResources)
	{
		this.childResources = ImmutableList.copyOf(childResources);
	}

	@Override
	public List<ListJavaFileItem> childResources()
	{
		return childResources;
	}

}
