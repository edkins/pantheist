package io.pantheist.common.api.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class AdditionalStructureItemImpl implements AdditionalStructureItem
{
	private final boolean literal;
	private final String name;
	private final boolean suggestHiding;

	@Inject
	private AdditionalStructureItemImpl(
			@Assisted("literal") @JsonProperty("literal") final boolean literal,
			@Assisted("name") @JsonProperty("name") final String name,
			@Assisted("suggestHiding") @JsonProperty("suggestHiding") final boolean suggestHiding)
	{
		this.literal = literal;
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.suggestHiding = suggestHiding;
	}

	@Override
	public boolean literal()
	{
		return literal;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public boolean suggestHiding()
	{
		return suggestHiding;
	}

}
