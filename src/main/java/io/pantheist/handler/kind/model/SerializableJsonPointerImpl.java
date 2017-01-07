package io.pantheist.handler.kind.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonPointer;

/**
 * This is just JsonPointer, but wrapped so it can be serialized as a JSON string.
 */
final class SerializableJsonPointerImpl implements SerializableJsonPointer
{
	private final JsonPointer pointer;

	@JsonCreator
	private SerializableJsonPointerImpl(final String path)
	{
		this.pointer = JsonPointer.compile(path);
	}

	@Override
	public String toString()
	{
		return pointer.toString();
	}

	@Override
	public JsonPointer pointer()
	{
		return pointer;
	}
}
