package io.pantheist.api.schema.backend;

import io.pantheist.common.util.Possible;

public interface SchemaBackend
{
	Possible<Void> validateAgainstJsonSchema(String schemaId, String text);

	Possible<String> postJsonSchema(String data);
}
