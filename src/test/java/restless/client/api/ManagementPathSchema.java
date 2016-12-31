package restless.client.api;

import restless.api.schema.model.ApiSchema;

public interface ManagementPathSchema
{
	ResponseType validate(String data, String contentType);

	String url();

	ManagementData data();

	ApiSchema describeSchema();

	ResponseType describeSchemaResponseType();

	void delete();

	ResponseType deleteResponseType();
}
