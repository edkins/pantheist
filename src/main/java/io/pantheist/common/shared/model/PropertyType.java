package io.pantheist.common.shared.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PropertyType
{
	BOOLEAN("boolean"),
	STRING("string"),
	ARRAY("array");

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

	public String simpleTypeToSql()
	{
		switch (this) {
		case STRING:
			return "varchar";
		case BOOLEAN:
			return "boolean";
		default:
			throw new IllegalArgumentException("Not a simple type, or not recognized: " + this);
		}
	}
}
