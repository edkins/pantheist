package io.pantheist.handler.kind.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = JsonPathImpl.class)
public interface JsonPath
{
	@Override
	@JsonValue
	String toString();
}
