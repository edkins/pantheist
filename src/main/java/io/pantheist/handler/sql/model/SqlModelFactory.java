package io.pantheist.handler.sql.model;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.shared.model.PropertyType;

public interface SqlModelFactory
{
	SqlProperty property(
			@Assisted("name") String name,
			@Assisted("type") PropertyType type,
			@Assisted("isPrimaryKey") boolean isPrimaryKey);
}
