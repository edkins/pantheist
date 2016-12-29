package restless.handler.schema.backend;

import java.util.List;
import java.util.Optional;

import restless.common.util.Possible;
import restless.handler.schema.model.SchemaComponent;

public interface JsonSchemaStore
{
	Possible<Void> putJsonSchema(String schemaId, String schemaText);

	Possible<String> getJsonSchema(String schemaId);

	Possible<Void> validateAgainstJsonSchema(String schemaId, String data);

	/**
	 * Returns component information if it exists, empty if it doesn't exist.
	 *
	 * Throws an exception if there's some other problem, e.g. the schema itself doesn't exist.
	 */
	Optional<SchemaComponent> getJsonSchemaComponent(String schemaId, String componentId);

	List<SchemaComponent> listComponents(String schemaId);
}
