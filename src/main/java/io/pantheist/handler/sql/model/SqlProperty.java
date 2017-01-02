package io.pantheist.handler.sql.model;

import javax.annotation.Nullable;

import io.pantheist.common.shared.model.PropertyType;
import io.pantheist.common.shared.model.TypeInfo;

public interface SqlProperty
{
	String name();

	PropertyType type();

	/**
	 * Type info for elements of an array type.
	 */
	@Nullable
	TypeInfo items();

	boolean isPrimaryKey();

	/**
	 * Just returns the same as isPrimaryKey for now.
	 */
	boolean isKey();

	String toSqlType();
}
