package restless.handler.schema.backend;

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
import com.google.inject.assistedinject.Assisted;

import restless.common.util.FailureReason;
import restless.common.util.Possible;
import restless.common.util.View;

final class ValidatorJsonImpl implements Validator
{
	private static final Logger LOGGER = LogManager.getLogger(ValidatorJsonImpl.class);
	private final ObjectMapper objectMapper;
	private final JsonSchemaFactory jsonSchemaFactory;
	private final JsonNode schemaJson;

	@Inject
	private ValidatorJsonImpl(
			final ObjectMapper objectMapper,
			final JsonSchemaFactory jsonSchemaFactory,
			@Assisted final JsonNode schemaJson)
	{
		this.objectMapper = checkNotNull(objectMapper);
		this.jsonSchemaFactory = checkNotNull(jsonSchemaFactory);
		this.schemaJson = checkNotNull(schemaJson);
	}

	@Override
	public Possible<Void> checkSchema()
	{
		final JsonNode metaSchema = SchemaVersion.DRAFTV4.getSchema();
		return checkJsonNodeAgainstSchema(metaSchema, schemaJson);
	}

	@Override
	public Possible<Void> validate(final String instanceData)
	{
		try
		{
			final JsonNode instanceJson = objectMapper.readValue(instanceData, JsonNode.class);
			return checkJsonNodeAgainstSchema(schemaJson, instanceJson);
		}
		catch (final IOException e)
		{
			LOGGER.catching(e);
			return FailureReason.REQUEST_HAS_INVALID_SYNTAX.happened();
		}
	}

	private Possible<Void> checkJsonNodeAgainstSchema(final JsonNode schemaJson, final JsonNode instanceJson)
	{
		try
		{
			final ProcessingReport report = jsonSchemaFactory.getJsonSchema(schemaJson)
					.validate(instanceJson);
			LOGGER.info(report);
			if (report.isSuccess())
			{
				return View.noContent();
			}
			else
			{
				return FailureReason.REQUEST_FAILED_SCHEMA.happened();
			}
		}
		catch (final ProcessingException e)
		{
			LOGGER.catching(e);
			return FailureReason.REQUEST_FAILED_SCHEMA.happened();
		}
	}
}
