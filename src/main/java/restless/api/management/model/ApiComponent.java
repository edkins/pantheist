package restless.api.management.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import restless.handler.schema.model.SchemaComponent;

@JsonDeserialize(as = ApiComponentImpl.class)
public interface ApiComponent
{
	@Nullable
	@JsonProperty("jsonSchema")
	SchemaComponent jsonSchema();
}
