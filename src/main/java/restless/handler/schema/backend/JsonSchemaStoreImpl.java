package restless.handler.schema.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import restless.common.util.FailureReason;
import restless.common.util.Possible;
import restless.common.util.View;
import restless.handler.filesystem.backend.FilesystemSnapshot;
import restless.handler.filesystem.backend.FilesystemStore;
import restless.handler.filesystem.backend.FsPath;

final class JsonSchemaStoreImpl implements JsonSchemaStore
{
	private final ObjectMapper objectMapper;
	private final SchemaBackendFactory factory;
	private final FilesystemStore filesystem;

	@Inject
	private JsonSchemaStoreImpl(
			final ObjectMapper objectMapper,
			final SchemaBackendFactory factory,
			final FilesystemStore filesystem)
	{
		this.objectMapper = checkNotNull(objectMapper);
		this.factory = checkNotNull(factory);
		this.filesystem = checkNotNull(filesystem);
	}

	@Override
	public Possible<Void> putJsonSchema(final String schemaId, final String schemaText)
	{
		final JsonNode schema;
		try
		{
			schema = objectMapper.readValue(schemaText, JsonNode.class);
		}
		catch (final IOException e)
		{
			return FailureReason.REQUEST_HAS_INVALID_SYNTAX.happened();
		}
		return factory.jsonValidator(schema).checkSchema().onSuccess(() -> {
			final FilesystemSnapshot snapshot = filesystem.snapshot();
			final FsPath file = path(schemaId);
			snapshot.willNeedDirectory(file.parent());
			snapshot.isFile(file);
			snapshot.writeSingleText(file, schemaText);
		});
	}

	private FsPath path(final String schemaId)
	{
		return filesystem.systemBucket().segment("json-schema").segment(schemaId);
	}

	@Override
	public Possible<String> getJsonSchema(final String schemaId)
	{
		final FilesystemSnapshot snapshot = filesystem.snapshot();
		final FsPath file = path(schemaId);
		if (snapshot.isFile(file))
		{
			return View.ok(snapshot.readText(file));
		}
		else
		{
			return FailureReason.DOES_NOT_EXIST.happened();
		}
	}

	@Override
	public Possible<Void> validateAgainstJsonSchema(final String schemaId, final String data)
	{
		return getJsonSchema(schemaId).posMap(schemaText -> {
			JsonNode schema;
			try
			{
				schema = objectMapper.readValue(schemaText, JsonNode.class);
			}
			catch (final IOException e)
			{
				return FailureReason.MISCONFIGURED.happened();
			}
			return factory.jsonValidator(schema).validate(data);
		});
	}
}
