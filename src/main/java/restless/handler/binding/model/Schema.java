package restless.handler.binding.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
}
