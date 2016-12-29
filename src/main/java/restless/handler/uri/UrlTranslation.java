package restless.handler.uri;

import restless.handler.java.model.JavaFileId;

public interface UrlTranslation
{
	String kindToUrl(String kindId);

	String kindFromUrl(String url);

	String jsonSchemaToUrl(String jsonSchemaId);

	String jsonSchemaFromUrl(String url);

	String javaToUrl(JavaFileId javaFileId);

	JavaFileId javaFromUrl(String url);

	String locationToUrl(String serverId, String locationId);
}
