package restless.api.schema.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import restless.common.util.Possible;
import restless.handler.schema.backend.JsonSchemaStore;

final class SchemaBackendImpl implements SchemaBackend
{
	private final JsonSchemaStore schemaStore;

	@Inject
	private SchemaBackendImpl(final JsonSchemaStore schemaStore)
	{
		this.schemaStore = checkNotNull(schemaStore);
	}

	@Override
	public Possible<Void> putJsonSchema(final String schemaId, final String schemaText)
	{
		return schemaStore.putJsonSchema(schemaId, schemaText);
	}

	@Override
	public Possible<String> getJsonSchema(final String schemaId)
	{
		return schemaStore.getJsonSchema(schemaId);
	}

	@Override
	public Possible<Void> validateAgainstJsonSchema(final String schemaId, final String text)
	{
		return schemaStore.validateAgainstJsonSchema(schemaId, text);
	}

}
