package io.pantheist.handler.sql.model;

import io.pantheist.common.shared.model.TypeInfo;

public interface SqlProperty
{
	String name();

	TypeInfo typeInfo();

	boolean isPrimaryKey();

	/**
	 * Just returns the same as isPrimaryKey for now.
	 */
	boolean isKey();

	String toSqlType();
}
