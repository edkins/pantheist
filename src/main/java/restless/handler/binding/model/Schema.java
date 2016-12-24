package restless.handler.binding.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = SchemaEmptyImpl.class, name = "empty"),
		@JsonSubTypes.Type(value = SchemaJsonImpl.class, name = "json") })
public interface Schema
{
	SchemaType type();

	@JsonIgnore
	String httpContentType();

	@JsonIgnore
	String contentAsString();

	/**
	 * @throws UnsupportedOperationException if not a json schema
	 */
	@JsonIgnore
	JsonNode jsonNode();
}
