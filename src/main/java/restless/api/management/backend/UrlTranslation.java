package restless.api.management.backend;

interface UrlTranslation
{
	String jsonSchemaToUrl(String jsonSchemaId);

	String jsonSchemaFromUrl(String url);

	String javaToUrl(String javaPkg, String javaFile);

	String javaPkgFromUrl(String url);

	String javaFileFromUrl(String url);
}
