package io.pantheist.common.shared.model;

import javax.inject.Named;

import com.google.inject.assistedinject.Assisted;

public interface CommonSharedModelFactory
{
	GenericProperty property(@Assisted("name") String name, PropertyType type);

	@Named("boolean")
	GenericPropertyValue booleanValue(
			@Assisted("name") final String name,
			@Assisted("value") final boolean value);

	@Named("string")
	GenericPropertyValue stringValue(
			@Assisted("name") final String name,
			@Assisted("value") final String value);
}
