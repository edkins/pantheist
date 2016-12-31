package io.pantheist.handler.java.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class JavaBindingImpl implements JavaBinding
{
	private final String location;

	@Inject
	private JavaBindingImpl(@Assisted("location") @JsonProperty("location") final String location)
	{
		this.location = OtherPreconditions.checkNotNullOrEmpty(location);
	}

	@Override
	public String location()
	{
		return location;
	}

}
