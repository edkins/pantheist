package io.pantheist.common.shared.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PropertyType
{
	BOOLEAN("boolean"),
	STRING("string"),
	ARRAY("array"),
	OBJECT("object");

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
