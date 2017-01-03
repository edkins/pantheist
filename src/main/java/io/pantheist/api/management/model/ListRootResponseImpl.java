package io.pantheist.api.management.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.common.util.OtherPreconditions;

final class ListRootResponseImpl implements ListRootResponse
{
	private final List<ListClassifierItem> childResources;
	private final String clientConfigUrl;

	@Inject
	private ListRootResponseImpl(
			@Assisted @JsonProperty("childResources") final List<ListClassifierItem> childResources,
			@Assisted("clientConfigUrl") @JsonProperty("clientConfigUrl") final String clientConfigUrl)
	{
		this.childResources = checkNotNull(childResources);
		this.clientConfigUrl = OtherPreconditions.checkNotNullOrEmpty(clientConfigUrl);
	}

	@Override
	public List<? extends ListClassifierItem> childResources()
	{
		return childResources;
	}

	@Override
	public String clientConfigUrl()
	{
		return clientConfigUrl;
	}

}
