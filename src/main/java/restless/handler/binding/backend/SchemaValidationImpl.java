package restless.handler.binding.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.SchemaVersion;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import restless.handler.binding.model.Schema;

final class SchemaValidationImpl implements SchemaValidation
{
	private static final Logger LOGGER = LogManager.getLogger(SchemaValidationImpl.class);
	private final ObjectMapper objectMapper;
	private final JsonSchemaFactory jsonSchemaFactory;

	@Inject
	private SchemaValidationImpl(final ObjectMapper objectMapper, final JsonSchemaFactory jsonSchemaFactory)
	{
		this.objectMapper = checkNotNull(objectMapper);
		this.jsonSchemaFactory = checkNotNull(jsonSchemaFactory);
	}

	@Override
	public PossibleEmpty checkSchema(final Schema schema)
	{
		return createValidator(schema).checkSchema();
	}

	@Override
	public PossibleEmpty validate(final Schema schema, final String data)
	{
		return createValidator(schema).validate(data);
	}

	private static interface Validator
	{
		public PossibleEmpty checkSchema();

		public PossibleEmpty validate(String data);
	}

	private static class AlwaysTrueValidator implements Validator
	{
		@Override
		public PossibleEmpty checkSchema()
		{
			return PossibleEmpty.ok();
		}

		@Override
		public PossibleEmpty validate(final String data)
		{
			return PossibleEmpty.ok();
		}
	}

	private class JsonValidator implements Validator
	{
		private final JsonNode schemaJson;

		private JsonValidator(final JsonNode schemaJson)
		{
			this.schemaJson = checkNotNull(schemaJson);
		}

		@Override
		public PossibleEmpty checkSchema()
		{
			final JsonNode metaSchema = SchemaVersion.DRAFTV4.getSchema();
			return checkJsonNodeAgainstSchema(metaSchema, schemaJson);
		}

		@Override
		public PossibleEmpty validate(final String instanceData)
		{
			try
			{
				final JsonNode instanceJson = objectMapper.readValue(instanceData, JsonNode.class);
				return checkJsonNodeAgainstSchema(schemaJson, instanceJson);
			}
			catch (final IOException e)
			{
				LOGGER.catching(e);
				return PossibleEmpty.requestHasInvalidSyntax();
			}
		}

		private PossibleEmpty checkJsonNodeAgainstSchema(final JsonNode schemaJson, final JsonNode instanceJson)
		{
			try
			{
				final ProcessingReport report = jsonSchemaFactory.getJsonSchema(schemaJson).validate(instanceJson);
				LOGGER.info(report);
				if (report.isSuccess())
				{
					return PossibleEmpty.ok();
				}
				else
				{
					return PossibleEmpty.requestFailedSchema();
				}
			}
			catch (final ProcessingException e)
			{
				LOGGER.catching(e);
				return PossibleEmpty.requestFailedSchema();
			}
		}
	}

	private Validator createValidator(final Schema schema)
	{
		switch (schema.type()) {
		case empty:
			return new AlwaysTrueValidator();
		case json:
			return new JsonValidator(schema.jsonNode());
		default:
			throw new UnsupportedOperationException("Unknown schema type " + schema.type());
		}
	}
}
