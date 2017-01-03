package io.pantheist.common.shared.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PropertyType
{
	BOOLEAN("boolean"),
	STRING("string"),
	STRING_ARRAY("string-array"),
	OBJECT_ARRAY("object-array");

	private final String name;

	private PropertyType(final String name)
	{
		this.name = name;
	}

	@Override
	@JsonValue
	public String toString()
	{
		return name;
	}
}
