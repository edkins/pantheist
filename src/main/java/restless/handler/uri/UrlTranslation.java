package restless.handler.uri;

import java.util.List;

import restless.handler.java.model.JavaFileId;

public interface UrlTranslation
{
	String kindToUrl(String kindId);

	String kindFromUrl(String url);

	String jsonSchemaToUrl(String jsonSchemaId);

	String jsonSchemaFromUrl(String url);

	String javaPkgToUrl(String pkg);

	String javaToUrl(JavaFileId javaFileId);

	JavaFileId javaFromUrl(String url);

	String locationToUrl(String serverId, String locationId);

	List<ListClassifierItem> listRootClassifiers();

	List<ListClassifierItem> listEntityClassifiers(String entityId);

	List<ListClassifierItem> listKindClassifiers(String kindId);

	List<ListClassifierItem> listJavaPkgClassifiers(String pkg);
}
