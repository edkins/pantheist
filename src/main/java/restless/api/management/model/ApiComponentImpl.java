package restless.api.management.model;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.assistedinject.Assisted;

import restless.handler.schema.model.SchemaComponent;

final class ApiComponentImpl implements ApiComponent
{
	private final SchemaComponent jsonSchema;

	@Inject
	public ApiComponentImpl(@Nullable @Assisted @JsonProperty("jsonSchema") final SchemaComponent jsonSchema)
	{
		this.jsonSchema = jsonSchema;
	}

	@Override
	public SchemaComponent jsonSchema()
	{
		return jsonSchema;
	}

}
