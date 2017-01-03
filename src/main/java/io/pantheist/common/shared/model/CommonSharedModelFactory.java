package io.pantheist.common.shared.model;

import java.util.Map;

import com.google.inject.assistedinject.Assisted;

public interface CommonSharedModelFactory
{
	TypeInfo typeInfo(
			PropertyType type,
			@Assisted("items") TypeInfo items,
			Map<String, TypeInfo> properties);
}
