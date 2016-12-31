package restless.api.java.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class ApiJavaBindingImpl implements ApiJavaBinding
{
	private final String location;

	@Inject
	private ApiJavaBindingImpl(@Assisted("location") @JsonProperty("location") final String location)
	{
		this.location = OtherPreconditions.checkNotNullOrEmpty(location);
	}

	@Override
	public String location()
	{
		return location;
	}

}
