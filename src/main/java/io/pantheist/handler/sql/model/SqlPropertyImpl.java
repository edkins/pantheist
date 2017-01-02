package io.pantheist.handler.sql.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.shared.model.PropertyType;
import io.pantheist.common.util.OtherPreconditions;

final class SqlPropertyImpl implements SqlProperty
{
	private final String name;
	private final PropertyType type;
	private final boolean isPrimaryKey;

	@Inject
	private SqlPropertyImpl(
			@Assisted("name") final String name,
			@Assisted("type") final PropertyType type,
			@Assisted("isPrimaryKey") final boolean isPrimaryKey)
	{
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.type = checkNotNull(type);
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

}
