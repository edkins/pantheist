package restless.handler.uri;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import restless.api.management.backend.UriPattern;
import restless.api.management.backend.UriPatternImpl;
import restless.handler.java.model.JavaFileId;
import restless.handler.java.model.JavaModelFactory;
import restless.system.config.RestlessConfig;

final class UrlTranslationImpl implements UrlTranslation
{
	private final UriPattern kind;
	private final UriPattern jsonSchema;
	private final UriPattern java;
	private final UriPattern location;
	private final JavaModelFactory javaFactory;

	@Inject
	private UrlTranslationImpl(final RestlessConfig config, final JavaModelFactory javaFactory)
	{
		final UriPattern root = UriPatternImpl
				.hostAndPort("http", "localhost:" + config.managementPort())
				.emptySegment();

		this.kind = root.segment("kind").var("kindId");
		this.jsonSchema = root.segment("json-schema").var("schemaId");
		this.java = root.segment("java-pkg").var("pkg").segment("file").var("file");
		this.location = root.segment("server").var("serverId").segment("location").var("locationId");
		this.javaFactory = checkNotNull(javaFactory);
	}

	@Override
	public String jsonSchemaToUrl(final String jsonSchemaId)
	{
		return jsonSchema.generate(ImmutableMap.of("schemaId", jsonSchemaId), false);
	}

	@Override
	public String jsonSchemaFromUrl(final String url)
	{
		return jsonSchema.getVar("schemaId", url);
	}

	@Override
	public String javaToUrl(final JavaFileId javaFileId)
	{
		return java.generate(ImmutableMap.of("pkg", javaFileId.pkg(), "file", javaFileId.file()), false);
	}

	@Override
	public JavaFileId javaFromUrl(@Nullable final String url)
	{
		return javaFactory.fileId(java.getVar("pkg", url), java.getVar("file", url));
	}

	@Override
	public String kindToUrl(final String kindId)
	{
		return kind.generate(ImmutableMap.of("kindId", kindId), false);
	}

	@Override
	public String kindFromUrl(final String url)
	{
		return kind.getVar("kindId", url);
	}

	@Override
	public String locationToUrl(final String serverId, final String locationId)
	{
		return location.generate(ImmutableMap.of("serverId", serverId, "locationId", locationId), false);
	}

}
