package restless.common.api.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class BindingActionImpl implements BindingAction
{
	private final String url;

	@Inject
	private BindingActionImpl(@Assisted("url") @JsonProperty("url") final String url)
	{
		this.url = OtherPreconditions.checkNotNullOrEmpty(url);
	}

	@Override
	public String url()
	{
		return url;
	}
}
