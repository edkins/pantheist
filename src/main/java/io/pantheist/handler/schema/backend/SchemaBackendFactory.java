package io.pantheist.handler.schema.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.assistedinject.Assisted;

interface SchemaBackendFactory
{
	Validator jsonValidator(@Assisted JsonNode schemaJson);
}
