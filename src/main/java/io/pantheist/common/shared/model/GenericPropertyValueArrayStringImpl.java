package io.pantheist.common.shared.model;

import java.util.List;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class GenericPropertyValueArrayStringImpl implements GenericPropertyValue
{
	private final String name;
	private final List<String> value;

	@Inject
	private GenericPropertyValueArrayStringImpl(
			@Assisted("name") @JsonProperty("name") final String name,
			@Assisted("value") @JsonProperty("value") final List<String> value)
	{
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.value = ImmutableList.copyOf(value);
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public PropertyType type()
	{
		return PropertyType.ARRAY;
	}

	@Override
	public boolean booleanValue()
	{
		throw new UnsupportedOperationException("Is an array, not a boolean");
	}

	@Override
	public String stringValue()
	{
		throw new UnsupportedOperationException("Is an array, not a string");
	}

	@Override
	public boolean matchesJsonNodeExactly(final JsonNode jsonNode)
	{
		return false;
	}

	@Override
	public PropertyType arrayItemType()
	{
		return PropertyType.STRING;
	}

	@Override
	public Object[] arrayValue()
	{
		return value.toArray(new Object[value.size()]);
	}

	@Override
	public boolean isArrayContainingJsonNode(final JsonNode jsonNode)
	{
		if (!jsonNode.isTextual())
		{
			return false;
		}
		for (final String item : value)
		{
			if (item.equals(jsonNode.textValue()))
			{
				return true;
			}
		}
		return false;
	}
}
