package io.pantheist.handler.sql.model;

import javax.annotation.Nullable;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.shared.model.PropertyType;
import io.pantheist.common.shared.model.TypeInfo;

public interface SqlModelFactory
{
	SqlProperty property(
			@Assisted("name") String name,
			@Assisted("type") PropertyType type,
			@Nullable @Assisted("items") TypeInfo items,
			@Assisted("isPrimaryKey") boolean isPrimaryKey);
}
