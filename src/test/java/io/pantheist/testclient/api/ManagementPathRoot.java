package io.pantheist.testclient.api;

import io.pantheist.api.flatdir.model.ListFlatDirResponse;
import io.pantheist.api.java.model.ListJavaPkgResponse;
import io.pantheist.api.kind.model.ListKindResponse;
import io.pantheist.api.sql.model.ListSqlTableResponse;
import io.pantheist.common.api.model.ListClassifierResponse;

public interface ManagementPathRoot
{
	/**
	 * Look up a server identified by port.
	 */
	ManagementPathServer server(int port);

	/**
	 * Returns a path to the data api for the given path. This can be used for
	 * setting and retrieving data for handlers that support that, such as the
	 * filesystem.
	 *
	 * @return data api
	 */
	ManagementData data(String path);

	/**
	 * Return a java package resource.
	 */
	ManagementPathJavaPackage javaPackage(String pkg);

	/**
	 * Return the json-schema with the given id.
	 */
	ManagementPathSchema jsonSchema(String schemaId);

	/**
	 * Returns an API for managing a kind.
	 *
	 * A kind is like a schema for entities. It specifies which handlers must be present
	 * and some restrictions on the structure of their associated files.
	 *
	 * For example, it might say that the java handler needs to be present and
	 * its associated file must be an interface, not a class.
	 */
	ManagementPathKind kind(String kindId);

	ListClassifierResponse listClassifiers();

	String urlOfService(String classifierSegment);

	ListJavaPkgResponse listJavaPackages();

	ListKindResponse listKinds();

	ManagementPathJavaBinding javaBinding();

	/**
	 * A view of the file system where directories are not presented hierarchically.
	 *
	 * Instead every directory path maps to a single path segment, and URL percent encoding
	 * is used to encode the directory separators as %2F
	 *
	 * As a special case, the root directory is mapped to a single %2F, not to the empty segment.
	 * This is to avoid accidental removal of empty URI segments. None of the other directories
	 * will start or end with an escaped slash.
	 */
	ManagementFlatDirPath flatDir(String dir);

	ListFlatDirResponse listFlatDirs();

	ManagementPathSqlTable sqlTable(String tableName);

	ListSqlTableResponse listSqlTables();

	ManagementPathEntities entitiesWithKind(String kindId);

	ListClassifierResponse listEntityClassifiers();
}
