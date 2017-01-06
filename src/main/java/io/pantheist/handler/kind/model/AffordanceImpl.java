package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import io.pantheist.common.util.OtherPreconditions;

final class AffordanceImpl implements Affordance
{
	private final AffordanceType type;
	private final String name;
	private final JsonPath location;
	private final JsonNode prototypeValue;

	public AffordanceImpl(
			@JsonProperty("type") final AffordanceType type,
			@JsonProperty("name") final String name,
			@JsonProperty("location") final JsonPath location,
			@JsonProperty("prototypeValue") final JsonNode prototypeValue)
	{
		this.type = checkNotNull(type);
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.location = checkNotNull(location);
		this.prototypeValue = checkNotNull(prototypeValue);
	}

	@Override
	public AffordanceType type()
	{
		return type;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public JsonPath location()
	{
		return location;
	}

	@Override
	public JsonNode prototypeValue()
	{
		return prototypeValue;
	}

}
