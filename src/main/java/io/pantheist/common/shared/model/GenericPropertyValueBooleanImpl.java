package io.pantheist.common.shared.model;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
	public TypeInfo typeInfo()
	{
		return TypeInfoImpl.BOOLEAN;
	}

	@Override
	public boolean booleanValue()
	{
		return value;
	}

	@Override
	public String stringValue()
	{
		throw new IllegalStateException("Is a boolean, not a string");
	}

	@Override
	public boolean matchesJsonNodeExactly(final JsonNode jsonNode)
	{
		return jsonNode.isBoolean() && jsonNode.booleanValue() == value;
	}

	@Override
	public boolean isArrayContainingJsonNode(final JsonNode jsonNode)
	{
		return false;
	}

	@Override
	public String toString()
	{
		return String.valueOf(value);
	}

	@Override
	public String jsonValue(final ObjectMapper objectMapper) throws JsonProcessingException
	{
		if (value)
		{
			return "true";
		}
		else
		{
			return "false";
		}
	}

	@Override
	public JsonNode toJsonNode(final JsonNodeFactory nodeFactory)
	{
		return nodeFactory.booleanNode(value);
	}
}
