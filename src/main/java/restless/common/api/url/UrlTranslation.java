package restless.common.api.url;

import java.util.List;

import restless.common.api.model.BindingAction;
import restless.common.api.model.CreateAction;
import restless.common.api.model.DataAction;
import restless.common.api.model.DeleteAction;
import restless.common.api.model.ListClassifierItem;
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

	String entityToUrl(String entityId);

	String componentToUrl(String entityId, String componentId);

	List<ListClassifierItem> listRootClassifiers();

	List<ListClassifierItem> listEntityClassifiers(String entityId);

	List<ListClassifierItem> listKindClassifiers(String kindId);

	List<ListClassifierItem> listJavaPkgClassifiers(String pkg);

	List<ListClassifierItem> listFlatDirClassifiers(String dir);

	CreateAction javaPkgCreateAction();

	DataAction javaFileDataAction(JavaFileId javaFileId);

	DeleteAction javaFileDeleteAction(JavaFileId javaFileId);

	CreateAction jsonSchemaCreateAction();

	DataAction jsonSchemaDataAction();

	DeleteAction jsonSchemaDeleteAction();

	BindingAction javaPkgBindingAction();

	String flatDirFileToUrl(String dir, String file);

	String flatDirToUrl(String dir);

}
