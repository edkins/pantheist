package restless.handler.schema.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Consumer;

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

import restless.common.util.AntiIterator;
import restless.common.util.FailureReason;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.schema.model.SchemaComponent;
import restless.handler.schema.model.SchemaModelFactory;

final class ValidatorJsonImpl implements Validator
{
	private static final String ROOT = ".";
	private static final Logger LOGGER = LogManager.getLogger(ValidatorJsonImpl.class);
	private final ObjectMapper objectMapper;
	private final JsonSchemaFactory jsonSchemaFactory;
	private final JsonNode schemaJson;
	private final SchemaModelFactory modelFactory;

	@Inject
	private ValidatorJsonImpl(
			final ObjectMapper objectMapper,
			final JsonSchemaFactory jsonSchemaFactory,
			@Assisted final JsonNode schemaJson,
			final SchemaModelFactory modelFactory)
	{
		this.objectMapper = checkNotNull(objectMapper);
		this.jsonSchemaFactory = checkNotNull(jsonSchemaFactory);
		this.schemaJson = checkNotNull(schemaJson);
		this.modelFactory = checkNotNull(modelFactory);
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

	private void findComponentRecursive(
			final boolean isRoot,
			final JsonNode node,
			final Consumer<SchemaComponent> consumer)
	{
		if (node.isObject() || node.isArray())
		{
			if (isRoot)
			{
				consumer.accept(modelFactory.component(ROOT, isRoot));
			}

			final JsonNode id = node.get("id");
			if (id != null && id.isTextual() && id.asText().startsWith("#"))
			{
				consumer.accept(modelFactory.component(id.asText().substring(1), isRoot));
			}

			final Iterator<JsonNode> iterator = node.elements();
			while (iterator.hasNext())
			{
				final JsonNode child = iterator.next();
				findComponentRecursive(false, child, consumer);
			}
		}
	}

	@Override
	public AntiIterator<SchemaComponent> components()
	{
		return consumer -> findComponentRecursive(true, schemaJson, consumer);
	}
}
