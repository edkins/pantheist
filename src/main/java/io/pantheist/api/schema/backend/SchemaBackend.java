package io.pantheist.api.schema.backend;

import io.pantheist.api.schema.model.ApiSchema;
import io.pantheist.api.schema.model.ListSchemaResponse;
import io.pantheist.common.util.Possible;

public interface SchemaBackend
{
	Possible<Void> putJsonSchema(String schemaId, String schemaText, boolean failIfExists);

	Possible<String> getJsonSchema(String schemaId);

	Possible<Void> validateAgainstJsonSchema(String schemaId, String text);

	ListSchemaResponse listSchemas();

	Possible<ApiSchema> describeJsonSchema(String schemaId);

	Possible<Void> deleteJsonSchema(String schemaId);

	Possible<String> postJsonSchema(String data);
}
