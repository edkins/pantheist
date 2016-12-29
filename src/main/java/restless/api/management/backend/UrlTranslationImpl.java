package restless.api.management.backend;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import restless.system.config.RestlessConfig;

final class UrlTranslationImpl implements UrlTranslation
{
	private final UriPattern jsonSchema;
	private final UriPattern java;

	@Inject
	private UrlTranslationImpl(final RestlessConfig config)
	{
		final UriPattern root = UriPatternImpl
				.hostAndPort("http", "localhost:" + config.managementPort())
				.emptySegment();

		this.jsonSchema = root.segment("json-schema").var("schemaId");
		this.java = root.segment("java-pkg").var("pkg").segment("file").var("file");
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
	public String javaToUrl(final String javaPkg, final String javaFile)
	{
		return java.generate(ImmutableMap.of("pkg", javaPkg, "file", javaFile), false);
	}

	@Override
	public String javaPkgFromUrl(@Nullable final String url)
	{
		if (url == null)
		{
			return null;
		}
		return java.getVar("pkg", url);
	}

	@Override
	public String javaFileFromUrl(final String url)
	{
		return java.getVar("file", url);
	}

}
