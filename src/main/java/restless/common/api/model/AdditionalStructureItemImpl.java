package restless.common.api.model;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.OtherPreconditions;

final class AdditionalStructureItemImpl implements AdditionalStructureItem
{
	private final boolean literal;
	private final String name;

	@Inject
	private AdditionalStructureItemImpl(
			@Assisted("literal") @JsonProperty("literal") final boolean literal,
			@Assisted("name") @JsonProperty("name") final String name)
	{
		this.literal = literal;
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
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

}
