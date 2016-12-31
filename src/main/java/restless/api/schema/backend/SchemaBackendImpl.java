package restless.api.schema.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;

import restless.api.schema.model.ApiSchema;
import restless.api.schema.model.ApiSchemaModelFactory;
import restless.api.schema.model.ListSchemaResponse;
import restless.common.api.url.UrlTranslation;
import restless.common.util.FailureReason;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.schema.backend.JsonSchemaStore;

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

	@Override
	public ListSchemaResponse listSchemas()
	{
		return schemaStore.listJsonSchemaIds()
				.map(urlTranslation::jsonSchemaToUrl)
				.map(modelFactory::listSchemaItem)
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
