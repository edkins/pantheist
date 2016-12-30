package restless.api.java.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import restless.common.api.model.AdditionalStructureItem;

final class ListJavaPkgResponseImpl implements ListJavaPkgResponse
{
	private final List<ListJavaPkgItem> childResources;
	private final List<AdditionalStructureItem> additionalStructure;

	@Inject
	private ListJavaPkgResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListJavaPkgItem> childResources,
			@Assisted @JsonProperty("additionalStructure") final List<AdditionalStructureItem> additionalStructure)
	{
		this.childResources = ImmutableList.copyOf(childResources);
		this.additionalStructure = checkNotNull(additionalStructure);
	}

	@Override
	public List<ListJavaPkgItem> childResources()
	{
		return childResources;
	}

	@Override
	public List<AdditionalStructureItem> additionalStructure()
	{
		return additionalStructure;
	}

}
