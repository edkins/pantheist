package restless.handler.uri;

public interface UrlTranslation
{
	String kindToUrl(String kindId);

	String kindFromUrl(String url);

	String jsonSchemaToUrl(String jsonSchemaId);

	String jsonSchemaFromUrl(String url);

	String javaToUrl(String javaPkg, String javaFile);

	String javaPkgFromUrl(String url);

	String javaFileFromUrl(String url);

	String locationToUrl(String serverId, String locationId);
}
