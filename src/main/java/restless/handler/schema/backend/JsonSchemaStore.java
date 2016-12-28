package restless.handler.schema.backend;

import restless.common.util.Possible;

public interface JsonSchemaStore
{
	Possible<Void> putJsonSchema(String schemaId, String schemaText);

	Possible<String> getJsonSchema(String schemaId);

	Possible<Void> validateAgainstJsonSchema(String schemaId, String data);
}
