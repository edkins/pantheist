package io.pantheist.api.entity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.pantheist.common.util.OtherPreconditions;

final class AddRequestImpl implements AddRequest
{
	private final String addName;

	private AddRequestImpl(@JsonProperty("addName") final String addName)
	{
		this.addName = OtherPreconditions.checkNotNullOrEmpty(addName);
	}

	@Override
	public String addName()
	{
		return addName;
	}

}
