package io.pantheist.handler.sql.model;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.shared.model.TypeInfo;

public interface SqlModelFactory
{
	SqlProperty property(
			@Assisted("name") String name,
			@Assisted TypeInfo typeInfo,
			@Assisted("isPrimaryKey") boolean isPrimaryKey);
}
