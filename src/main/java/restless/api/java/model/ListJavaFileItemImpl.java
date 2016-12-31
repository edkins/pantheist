package restless.api.java.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class ListJavaFileItemImpl implements ListJavaFileItem
{
	private final String url;

	@Inject
	private ListJavaFileItemImpl(@Assisted("url") @JsonProperty("url") final String url)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
	}

	@Override
	public String url()
	{
		return url;
	}

}
