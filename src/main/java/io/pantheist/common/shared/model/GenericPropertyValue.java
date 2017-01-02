package io.pantheist.common.shared.model;

import com.fasterxml.jackson.databind.JsonNode;

public interface GenericPropertyValue
{
	String name();

	PropertyType type();

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
}
