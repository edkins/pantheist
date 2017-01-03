package io.pantheist.handler.sql.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.shared.model.TypeInfo;
import io.pantheist.common.util.OtherPreconditions;

final class SqlPropertyImpl implements SqlProperty
{
	private final String name;
	private final TypeInfo typeInfo;
	private final boolean isPrimaryKey;

	@Inject
	private SqlPropertyImpl(
			@Assisted("name") final String name,
			@Assisted final TypeInfo typeInfo,
			@Assisted("isPrimaryKey") final boolean isPrimaryKey)
	{
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.typeInfo = checkNotNull(typeInfo);
		this.isPrimaryKey = isPrimaryKey;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public TypeInfo typeInfo()
	{
		return typeInfo;
	}

	@Override
	public boolean isPrimaryKey()
	{
		return isPrimaryKey;
	}

	@Override
	public String toSqlType()
	{
		switch (typeInfo.type()) {
		case BOOLEAN:
			return "boolean";
		case STRING:
			return "varchar";
		case STRING_ARRAY:
		case OBJECT_ARRAY:
			return "jsonb";
		default:
			throw new UnsupportedOperationException("Unknown type to convert to sql: " + typeInfo.type());
		}
	}

	@Override
	public boolean isKey()
	{
		return isPrimaryKey;
	}
}
