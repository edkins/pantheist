package io.pantheist.common.api.url;

import java.util.List;

import io.pantheist.common.api.model.BindingAction;
import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.DataAction;
import io.pantheist.common.api.model.DeleteAction;
import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.common.api.model.ReplaceAction;
import io.pantheist.handler.java.model.JavaFileId;

public interface UrlTranslation
{
	// Converting id's to and from url's
	String kindToUrl(String kindId);

	String kindFromUrl(String url);

	String entityToUrl(String kindId, String entityId);

	String jsonSchemaToUrl(String jsonSchemaId);

	String jsonSchemaFromUrl(String url);

	String javaPkgToUrl(String pkg);

	String javaToUrl(JavaFileId javaFileId);

	JavaFileId javaFromUrl(String url);

	String locationToUrl(String serverId, String locationId);

	String flatDirFileToUrl(String dir, String file);

	String flatDirToUrl(String dir);

	String sqlTableToUrl(String table);

	String sqlTableColumnToUrl(String table, String column);

	String sqlRowToUrl(String table, String column, String row);

	// Listing classifiers
	List<ListClassifierItem> listRootClassifiers();

	List<ListClassifierItem> listKindClassifiers(String kindId);

	List<ListClassifierItem> listJavaPkgClassifiers(String pkg);

	List<ListClassifierItem> listFlatDirClassifiers(String dir);

	// Obtaining actions
	CreateAction javaPkgCreateAction();

	DataAction javaFileDataAction(JavaFileId javaFileId);

	DeleteAction javaFileDeleteAction(JavaFileId javaFileId);

	CreateAction jsonSchemaCreateAction();

	DataAction jsonSchemaDataAction();

	DeleteAction jsonSchemaDeleteAction();

	BindingAction javaPkgBindingAction();

	CreateAction kindCreateAction();

	ReplaceAction listKindReplaceAction(String kindId);

	DataAction sqlRowDataAction(String table, String column, String row);

}
