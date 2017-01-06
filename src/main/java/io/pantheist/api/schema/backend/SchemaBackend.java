package io.pantheist.api.schema.backend;

import io.pantheist.api.schema.model.ListSchemaResponse;
import io.pantheist.common.api.model.Kinded;
import io.pantheist.common.util.Possible;

public interface SchemaBackend
{
	Possible<Void> putJsonSchema(String schemaId, String schemaText, boolean failIfExists);

	Possible<Kinded<String>> getJsonSchema(String schemaId);

	Possible<Void> validateAgainstJsonSchema(String schemaId, String text);

	ListSchemaResponse listSchemas();

	Possible<Void> deleteJsonSchema(String schemaId);

	Possible<String> postJsonSchema(String data);
}
