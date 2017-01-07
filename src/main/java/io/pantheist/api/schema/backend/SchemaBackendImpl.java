package io.pantheist.api.schema.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pantheist.api.management.backend.UrlPatternMismatchException;
import io.pantheist.api.schema.model.JustSchemaId;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.handler.schema.backend.JsonSchemaStore;

final class SchemaBackendImpl implements SchemaBackend
{
	private static final Logger LOGGER = LogManager.getLogger(SchemaBackendImpl.class);
	private final JsonSchemaStore schemaStore;
	private final UrlTranslation urlTranslation;
	private final ObjectMapper objectMapper;

	@Inject
	private SchemaBackendImpl(
			final JsonSchemaStore schemaStore,
			final UrlTranslation urlTranslation,
			final ObjectMapper objectMapper)
	{
		this.schemaStore = checkNotNull(schemaStore);
		this.urlTranslation = checkNotNull(urlTranslation);
		this.objectMapper = checkNotNull(objectMapper);
	}

	@Override
	public Possible<Void> validateAgainstJsonSchema(final String schemaId, final String text)
	{
		return schemaStore.validateAgainstJsonSchema(schemaId, text);
	}

	@Override
	public Possible<String> postJsonSchema(final String schemaText)
	{
		final JustSchemaId justId;
		try
		{
			justId = objectMapper.readValue(schemaText, JustSchemaId.class);
		}
		catch (final IOException e)
		{
			LOGGER.catching(e);
			return FailureReason.REQUEST_HAS_INVALID_SYNTAX.happened();
		}

		if (justId.id() == null)
		{
			return FailureReason.WRONG_LOCATION.happened();
		}

		final String schemaId;
		try
		{
			schemaId = urlTranslation.jsonSchemaFromUrl(justId.id());
		}
		catch (final UrlPatternMismatchException e)
		{
			LOGGER.catching(e);
			return FailureReason.WRONG_LOCATION.happened();
		}

		return schemaStore.putJsonSchema(schemaId, schemaText, true).map(x -> urlTranslation.jsonSchemaToUrl(schemaId));
	}

}
