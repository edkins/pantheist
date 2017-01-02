package io.pantheist.common.shared.model;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class GenericPropertyValueBooleanImpl implements GenericPropertyValue
{
	private final String name;
	private final boolean value;

	@Inject
	private GenericPropertyValueBooleanImpl(
			@Assisted("name") final String name,
			@Assisted("value") final boolean value)
	{
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.value = value;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public PropertyType type()
	{
		return PropertyType.BOOLEAN;
	}

	@Override
	public boolean booleanValue()
	{
		return value;
	}

	@Override
	public String stringValue()
	{
		throw new IllegalStateException("Is a boolean not a string");
	}

}
