package restless.api.schema.backend;

import restless.api.schema.model.ApiSchema;
import restless.api.schema.model.ListSchemaResponse;
import restless.common.util.Possible;

public interface SchemaBackend
{
	Possible<Void> putJsonSchema(String schemaId, String schemaText);

	Possible<String> getJsonSchema(String schemaId);

	Possible<Void> validateAgainstJsonSchema(String schemaId, String text);

	ListSchemaResponse listSchemas();

	Possible<ApiSchema> describeJsonSchema(String schemaId);

	Possible<Void> deleteJsonSchema(String schemaId);
}
