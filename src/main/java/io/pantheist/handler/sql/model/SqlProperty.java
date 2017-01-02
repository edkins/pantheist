package io.pantheist.handler.sql.model;

import io.pantheist.common.shared.model.PropertyType;

public interface SqlProperty
{
	String name();

	PropertyType type();

	boolean isPrimaryKey();
}
