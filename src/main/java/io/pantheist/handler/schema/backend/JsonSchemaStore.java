package io.pantheist.handler.schema.backend;

import java.util.List;
import java.util.Optional;

import io.pantheist.common.util.AntiIterator;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.schema.model.SchemaComponent;

public interface JsonSchemaStore
{
	Possible<Void> putJsonSchema(String schemaId, String schemaText, boolean failIfExists);

	Possible<String> getJsonSchema(String schemaId);

	Possible<Void> validateAgainstJsonSchema(String schemaId, String data);

	/**
	 * Returns component information if it exists, empty if it doesn't exist.
	 *
	 * Throws an exception if there's some other problem, e.g. the schema itself doesn't exist.
	 */
	Optional<SchemaComponent> getJsonSchemaComponent(String schemaId, String componentId);

	List<SchemaComponent> listComponents(String schemaId);

	AntiIterator<String> listJsonSchemaIds();

	boolean jsonSchemaExists(String schemaId);

	boolean deleteJsonSchema(String schemaId);
}
