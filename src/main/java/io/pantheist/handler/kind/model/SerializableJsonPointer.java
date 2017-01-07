package io.pantheist.handler.kind.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = SerializableJsonPointerImpl.class)
public interface SerializableJsonPointer
{
	@Override
	@JsonValue
	String toString();

	@JsonIgnore
	JsonPointer pointer();
}
