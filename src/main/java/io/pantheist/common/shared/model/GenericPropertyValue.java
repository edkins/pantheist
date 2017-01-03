package io.pantheist.common.shared.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public interface GenericPropertyValue
{
	String name();

	TypeInfo typeInfo();

	/**
	 * @throws IllegalStateException if type is not boolean
	 */
	boolean booleanValue();

	/**
	 * @throws IllegalStateException if type is not string
	 */
	String stringValue();

	/**
	 * Return whether this value matches a value specified as a json node.
	 *
	 * Both the type and value must match.
	 */
	boolean matchesJsonNodeExactly(JsonNode jsonNode);

	boolean isArrayContainingJsonNode(JsonNode jsonNode);

	String jsonValue(ObjectMapper objectMapper) throws JsonProcessingException;

	JsonNode toJsonNode(JsonNodeFactory nodeFactory);
}
