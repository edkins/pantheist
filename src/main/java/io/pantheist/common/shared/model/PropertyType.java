package io.pantheist.common.shared.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PropertyType
{
	BOOLEAN("boolean", "BOOLEAN"),
	STRING("string", "VARCHAR");

	private final String name;
	private final String sql;

	private PropertyType(final String name, final String sql)
	{
		this.name = name;
		this.sql = sql;
	}

	@Override
	@JsonValue
	public String toString()
	{
		return name;
	}

	public String sql()
	{
		return sql;
	}
}
