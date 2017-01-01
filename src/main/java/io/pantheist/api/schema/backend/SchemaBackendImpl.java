package io.pantheist.api.schema.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import io.pantheist.api.schema.model.ApiSchema;
import io.pantheist.api.schema.model.ApiSchemaModelFactory;
import io.pantheist.api.schema.model.ListSchemaItem;
import io.pantheist.api.schema.model.ListSchemaResponse;
import io.pantheist.common.api.url.UrlTranslation;
import io.pantheist.common.util.FailureReason;
import io.pantheist.common.util.Possible;
import io.pantheist.common.util.View;
import io.pantheist.handler.schema.backend.JsonSchemaStore;

final class SchemaBackendImpl implements SchemaBackend
{
	private final JsonSchemaStore schemaStore;
	private final UrlTranslation urlTranslation;
	private final ApiSchemaModelFactory modelFactory;

	@Inject
	private SchemaBackendImpl(
			final JsonSchemaStore schemaStore,
			final ApiSchemaModelFactory modelFactory,
			final UrlTranslation urlTranslation)
	{
		this.schemaStore = checkNotNull(schemaStore);
		this.modelFactory = checkNotNull(modelFactory);
		this.urlTranslation = checkNotNull(urlTranslation);
	}

	@Override
	public Possible<Void> putJsonSchema(final String schemaId, final String schemaText)
	{
		return schemaStore.putJsonSchema(schemaId, schemaText, false);
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

	ListSchemaItem toListSchemaItem(final String url)
	{
		return modelFactory.listSchemaItem(url, urlTranslation.kindToUrl("json-schema"));
	}

	@Override
	public ListSchemaResponse listSchemas()
	{
		return schemaStore.listJsonSchemaIds()
				.map(urlTranslation::jsonSchemaToUrl)
				.map(this::toListSchemaItem)
				.wrap(xs -> modelFactory.listSchemaResponse(xs, urlTranslation.jsonSchemaCreateAction()));
	}

	@Override
	public Possible<ApiSchema> describeJsonSchema(final String schemaId)
	{
		if (schemaStore.jsonSchemaExists(schemaId))
		{
			return View.ok(modelFactory.apiSchema(
					urlTranslation.jsonSchemaDataAction(),
					urlTranslation.jsonSchemaDeleteAction()));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<Void> deleteJsonSchema(final String schemaId)
	{
		if (schemaStore.deleteJsonSchema(schemaId))
		{
			return View.noContent();
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

}
