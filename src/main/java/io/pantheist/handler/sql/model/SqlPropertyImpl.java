package io.pantheist.handler.sql.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.shared.model.PropertyType;
import io.pantheist.common.shared.model.TypeInfo;
import io.pantheist.common.util.OtherPreconditions;

final class SqlPropertyImpl implements SqlProperty
{
	private final String name;
	private final PropertyType type;
	private final boolean isPrimaryKey;
	private final TypeInfo items;

	@Inject
	private SqlPropertyImpl(
			@Assisted("name") final String name,
			@Assisted("type") final PropertyType type,
			@Nullable @Assisted("items") final TypeInfo items,
			@Assisted("isPrimaryKey") final boolean isPrimaryKey)
	{
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.type = checkNotNull(type);
		this.items = items;
		this.isPrimaryKey = isPrimaryKey;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public PropertyType type()
	{
		return type;
	}

	@Override
	public boolean isPrimaryKey()
	{
		return isPrimaryKey;
	}

	@Override
	public TypeInfo items()
	{
		return items;
	}

	@Override
	public String toSqlType()
	{
		switch (type) {
		case ARRAY:
			return items.type().simpleTypeToSql() + "[]";
		default:
			return type.simpleTypeToSql();
		}
	}

	@Override
	public boolean isKey()
	{
		return isPrimaryKey;
	}
}
