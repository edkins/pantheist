package io.pantheist.handler.kind.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

final class AffordanceImpl implements Affordance
{
	private final AffordanceType type;
	private final String name;
	private final SerializableJsonPointer location;
	private final JsonNode prototypeValue;

	public AffordanceImpl(
			@JsonProperty("type") final AffordanceType type,
			@Nullable @JsonProperty("name") final String name,
			@Nullable @JsonProperty("location") final SerializableJsonPointer location,
			@JsonProperty("prototypeValue") final JsonNode prototypeValue)
	{
		this.type = checkNotNull(type);
		this.name = name;
		this.location = location;
		this.prototypeValue = prototypeValue;
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
	public SerializableJsonPointer location()
	{
		return location;
	}

	@Override
	public JsonNode prototypeValue()
	{
		return prototypeValue;
	}

}
