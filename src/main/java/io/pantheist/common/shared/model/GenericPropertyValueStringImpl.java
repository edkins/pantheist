package io.pantheist.common.shared.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class GenericPropertyValueStringImpl implements GenericPropertyValue
{
	private final String name;
	private final String value;

	@Inject
	private GenericPropertyValueStringImpl(@Assisted("name") final String name, @Assisted("value") final String value)
	{
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.value = checkNotNull(value);
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public PropertyType type()
	{
		return PropertyType.STRING;
	}

	@Override
	public boolean booleanValue()
	{
		throw new IllegalStateException("Is a string, not a boolean");
	}

	@Override
	public String stringValue()
	{
		return value;
	}

	@Override
	public boolean matchesJsonNodeExactly(final JsonNode jsonNode)
	{
		return jsonNode.isTextual() && value.equals(jsonNode.textValue());
	}

}
