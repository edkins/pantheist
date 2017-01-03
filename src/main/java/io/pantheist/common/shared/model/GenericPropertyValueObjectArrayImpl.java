package io.pantheist.common.shared.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.inject.assistedinject.Assisted;

import io.pantheist.common.util.OtherPreconditions;

final class GenericPropertyValueObjectArrayImpl implements GenericPropertyValue
{
	private final String name;
	private final TypeInfo typeInfo;
	private final ArrayNode value;

	@Inject
	private GenericPropertyValueObjectArrayImpl(
			@Assisted("name") final String name,
			@Assisted final TypeInfo typeInfo,
			@Assisted final ArrayNode jsonNode)
	{
		this.typeInfo = checkNotNull(typeInfo);
		this.name = OtherPreconditions.checkNotNullOrEmpty(name);
		this.value = checkNotNull(jsonNode);
		if (typeInfo.type() != PropertyType.OBJECT_ARRAY)
		{
			throw new IllegalArgumentException("type must be object-array");
		}
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public TypeInfo typeInfo()
	{
		return typeInfo;
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
		return value.equals(jsonNode);
	}

	@Override
	public boolean isArrayContainingJsonNode(final JsonNode jsonNode)
	{
		for (final JsonNode node : value)
		{
			if (node.equals(jsonNode))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String jsonValue(final ObjectMapper objectMapper) throws JsonProcessingException
	{
		return objectMapper.writeValueAsString(value);
	}

	@Override
	public JsonNode toJsonNode(final JsonNodeFactory nodeFactory)
	{
		return value;
	}

}
