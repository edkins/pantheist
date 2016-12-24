package restless.handler.binding.model;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Throwables;
import com.google.inject.assistedinject.Assisted;

import restless.common.util.DummyDeserializer;

final class SchemaJsonImpl implements Schema
{
	private final JsonNode schema;
	private final ObjectMapper objectMapper;

	@Inject
	SchemaJsonImpl(@JacksonInject @JsonDeserialize(using = DummyDeserializer.class) final ObjectMapper objectMapper,
			@Assisted @JsonProperty("schema") final JsonNode schema)
	{
		this.objectMapper = checkNotNull(objectMapper);
		this.schema = checkNotNull(schema);
	}

	@Override
	public SchemaType type()
	{
		return SchemaType.json;
	}

	@JsonProperty("schema")
	private JsonNode schema()
	{
		return schema;
	}

	@Override
	public String httpContentType()
	{
		return "application/schema+json";
	}

	@Override
	public String contentAsString()
	{
		try
		{
			return objectMapper.writeValueAsString(schema);
		}
		catch (final JsonProcessingException e)
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public JsonNode jsonNode()
	{
		return schema;
	}

}
