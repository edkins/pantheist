package io.pantheist.common.shared.model;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

import com.google.inject.assistedinject.Assisted;

public interface CommonSharedModelFactory
{
	GenericProperty property(
			@Assisted("name") String name,
			PropertyType type,
			@Nullable @Assisted("items") TypeInfo items);

	@Named("boolean")
	GenericPropertyValue booleanValue(
			@Assisted("name") final String name,
			@Assisted("value") final boolean value);

	@Named("string")
	GenericPropertyValue stringValue(
			@Assisted("name") final String name,
			@Assisted("value") final String value);

	@Named("arrayString")
	GenericPropertyValue arrayStringValue(
			@Assisted("name") final String name,
			@Assisted("value") final List<String> value);

	TypeInfo typeInfo(PropertyType type);
}
