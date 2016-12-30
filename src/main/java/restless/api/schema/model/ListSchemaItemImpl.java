package restless.api.schema.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class ListSchemaItemImpl implements ListSchemaItem
{
	private final String url;

	@Inject
	private ListSchemaItemImpl(@Assisted("url") @JsonProperty("url") final String url)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
	}

	@Override
	public String url()
	{
		return url;
	}

}
