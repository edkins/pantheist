package io.pantheist.testclient.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;

import io.pantheist.api.entity.model.ListEntityResponse;
import io.pantheist.api.flatdir.model.ApiFlatDirFile;
import io.pantheist.api.flatdir.model.ListFileResponse;
import io.pantheist.api.flatdir.model.ListFlatDirResponse;
import io.pantheist.api.java.model.ApiJavaBinding;
import io.pantheist.api.java.model.ListJavaFileResponse;
import io.pantheist.api.java.model.ListJavaPkgResponse;
import io.pantheist.api.management.model.ListConfigItem;
import io.pantheist.api.management.model.ListConfigResponse;
import io.pantheist.api.sql.model.ApiSqlRow;
import io.pantheist.api.sql.model.ListRowResponse;
import io.pantheist.api.sql.model.ListSqlTableResponse;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.handler.kind.model.Kind;
import io.pantheist.testclient.api.ManagementData;
import io.pantheist.testclient.api.ManagementFlatDirFilePath;
import io.pantheist.testclient.api.ManagementFlatDirPath;
import io.pantheist.testclient.api.ManagementPathEntities;
import io.pantheist.testclient.api.ManagementPathJavaBinding;
import io.pantheist.testclient.api.ManagementPathJavaFile;
import io.pantheist.testclient.api.ManagementPathJavaPackage;
import io.pantheist.testclient.api.ManagementPathKind;
import io.pantheist.testclient.api.ManagementPathLocation;
import io.pantheist.testclient.api.ManagementPathRoot;
import io.pantheist.testclient.api.ManagementPathSchema;
import io.pantheist.testclient.api.ManagementPathServer;
import io.pantheist.testclient.api.ManagementPathSqlRow;
import io.pantheist.testclient.api.ManagementPathSqlTable;
import io.pantheist.testclient.api.ManagementPathUnknownEntity;
import io.pantheist.testclient.api.ResponseType;

final class ManagementPathImpl implements
		ManagementPathServer,
		ManagementPathLocation,
		ManagementPathRoot,
		ManagementPathJavaPackage,
		ManagementPathKind,
		ManagementPathJavaFile,
		ManagementPathSchema,
		ManagementPathJavaBinding,
		ManagementFlatDirPath,
		ManagementPathSqlTable,
		ManagementPathSqlRow,
		ManagementFlatDirFilePath,
		ManagementPathUnknownEntity,
		ManagementPathEntities
{
	// Path segments
	private static final String JAVA_PKG = "java-pkg";
	private static final String FILE = "file";
	private static final String DATA = "data";
	private static final String LOCATION = "location";
	private static final String SERVER = "server";
	private static final String JSON_SCHEMA = "json-schema";
	private static final String ENTITY = "entity";
	private static final String KIND = "kind";
	private static final String VALIDATE = "validate";
	private static final String JAVA_BINDING = "java-binding";
	private static final String FLAT_DIR = "flat-dir";
	private static final String SQL_TABLE = "sql-table";

	// Content types
	private static final String APPLICATION_JSON = "application/json";
	private static final String TEXT_PLAIN = "text/plain";
	private static final String JSON_SCHEMA_MIME = "application/schema+json";

	// Collaborators
	private final TargetWrapper target;

	ManagementPathImpl(final TargetWrapper target)
	{
		this.target = checkNotNull(target);
	}

	@Override
	public ManagementPathServer server(final int port)
	{
		return new ManagementPathImpl(target.withSegment(SERVER).withSegment(String.valueOf(port)));
	}

	@Override
	public ManagementPathLocation location(final String path)
	{
		return new ManagementPathImpl(target.withSegment(LOCATION).withEscapedSegment(path));
	}

	@Override
	public void bindToFilesystem()
	{
		final Map<String, Object> map = new HashMap<>();
		target.putObjectAsJson(map);
	}

	@Override
	public void bindToExternalFiles(final String absolutePath)
	{
		final Map<String, Object> map = new HashMap<>();
		map.put("alias", absolutePath);
		target.putObjectAsJson(map);
	}

	@Override
	public void delete()
	{
		target.delete();
	}

	@Override
	public boolean exists()
	{
		return target.exists(APPLICATION_JSON);
	}

	@Override
	public List<ListConfigItem> listLocations()
	{
		return target.withSegment(LOCATION).getJson(ListConfigResponse.class).childResources();
	}

	@Override
	public String url()
	{
		return target.url();
	}

	@Override
	public ManagementData data(final String path)
	{
		return new ManagementDataImpl(target.withSegment(DATA).withSlashSeparatedSegments(path));
	}

	@Override
	public ManagementPathJavaFile file(final String file)
	{
		return new ManagementPathImpl(target.withSegment(FILE).withSegment(file));
	}

	@Override
	public ManagementPathJavaPackage javaPackage(final String pkg)
	{
		return new ManagementPathImpl(target.withSegment(JAVA_PKG).withSegment(pkg));
	}

	@Override
	public ManagementPathSchema jsonSchema(final String schemaId)
	{
		return new ManagementPathImpl(target.withSegment(ENTITY).withSegment(JSON_SCHEMA).withSegment(schemaId));
	}

	@Override
	public ManagementPathKind kind(final String kindId)
	{
		return new ManagementPathImpl(target.withSegment(ENTITY).withSegment(KIND).withSegment(kindId));
	}

	@Override
	public Kind getKind()
	{
		return target.getJson(Kind.class);
	}

	@Override
	public ManagementData data()
	{
		return new ManagementDataImpl(target.withSegment(DATA));
	}

	@Override
	public ListEntityResponse listEntities()
	{
		return target.getJson(ListEntityResponse.class);
	}

	@Override
	public ListClassifierResponse listClassifiers()
	{
		return target.getJson(ListClassifierResponse.class);
	}

	@Override
	public String urlOfService(final String classifierSegment)
	{
		return target.withSegment(classifierSegment).url();
	}

	@Override
	public ResponseType listClassifierResponseType()
	{
		return target.getResponseType(APPLICATION_JSON);
	}

	@Override
	public ListJavaPkgResponse listJavaPackages()
	{
		return target.withSegment(JAVA_PKG).getJson(ListJavaPkgResponse.class);
	}

	@Override
	public ListJavaFileResponse listJavaFiles()
	{
		return target.withSegment(FILE).getJson(ListJavaFileResponse.class);
	}

	@Override
	public ResponseType getJavaFileResponseType()
	{
		return target.getResponseType(TEXT_PLAIN);
	}

	@Override
	public ResponseType deleteResponseType()
	{
		return target.deleteResponseType();
	}

	@Override
	public ResponseType validate(final String data, final String contentType)
	{
		return target.withSegment(VALIDATE).postResponseType(data, contentType);
	}

	@Override
	public String getJsonSchemaString()
	{
		return target.getString(JSON_SCHEMA_MIME);
	}

	@Override
	public ResponseType getJsonSchemaResponseType()
	{
		return target.getResponseType(JSON_SCHEMA_MIME);
	}

	@Override
	public ManagementPathJavaBinding javaBinding()
	{
		return new ManagementPathImpl(target.withSegment(JAVA_BINDING));
	}

	@Override
	public ApiJavaBinding getJavaBinding()
	{
		return target.getJson(ApiJavaBinding.class);
	}

	@Override
	public void setJavaBinding(final String location)
	{
		final Map<String, Object> map = new HashMap<>();
		map.put("location", location);
		target.putObjectAsJson(map);
	}

	@Override
	public ManagementFlatDirPath flatDir(final String dir)
	{
		return new ManagementPathImpl(target.withSegment(FLAT_DIR).withEscapedSegment(dir));
	}

	@Override
	public ListFileResponse listFlatDirFiles()
	{
		return target.withSegment(FILE).getJson(ListFileResponse.class);
	}

	@Override
	public ListFlatDirResponse listFlatDirs()
	{
		return target.withSegment(FLAT_DIR).getJson(ListFlatDirResponse.class);
	}

	@Override
	public ManagementPathSqlTable sqlTable(final String tableName)
	{
		return new ManagementPathImpl(target.withSegment(SQL_TABLE).withSegment(tableName));
	}

	@Override
	public ListSqlTableResponse listSqlTables()
	{
		return target.withSegment(SQL_TABLE).getJson(ListSqlTableResponse.class);
	}

	@Override
	public ListRowResponse listBy(final String indexColumn)
	{
		return target.withSegment(indexColumn).getJson(ListRowResponse.class);
	}

	@Override
	public ResponseType listByResponseType(final String indexColumn)
	{
		return target.withSegment(indexColumn).getResponseType(APPLICATION_JSON);
	}

	@Override
	public ManagementPathSqlRow row(final String indexColumn, final String indexValue)
	{
		return new ManagementPathImpl(target.withSegment(indexColumn).withSegment(indexValue));
	}

	@Override
	public ResponseType getSqlRowResponseType()
	{
		return target.getResponseType(APPLICATION_JSON);
	}

	@Override
	public ApiSqlRow getSqlRow()
	{
		return target.getJson(ApiSqlRow.class);
	}

	@Override
	public ManagementFlatDirFilePath flatDirFile(final String filename)
	{
		return new ManagementPathImpl(target.withSegment(FILE).withSegment(filename));
	}

	@Override
	public ApiFlatDirFile describeFlatDirFile()
	{
		return target.getJson(ApiFlatDirFile.class);
	}

	@Override
	public ResponseType getFlatDirFileResponseType()
	{
		return target.getResponseType(APPLICATION_JSON);
	}

	@Override
	public String postCreate(final String data, final String contentType)
	{
		return target.withSegment("create").postAndGetPath(data, contentType).url();
	}

	@Override
	public ResponseType postCreateResponseType(final String data, final String contentType)
	{
		return target.withSegment("create").postResponseType(data, contentType);
	}

	@Override
	public String headKindUrl()
	{
		return target.headLink("type");
	}

	@Override
	public String getJava()
	{
		return target.getString(TEXT_PLAIN);
	}

	@Override
	public void putJavaResource(final String resourcePath)
	{
		target.putResource(resourcePath, TEXT_PLAIN);
	}

	@Override
	public ResponseType putJavaResourceResponseType(final String resourcePath)
	{
		return target.putResourceResponseType(resourcePath, TEXT_PLAIN);
	}

	@Override
	public void putKindResource(final String resourcePath)
	{
		target.putResource(resourcePath, APPLICATION_JSON);
	}

	@Override
	public ResponseType putKindResourceResponseType(final String resourcePath)
	{
		return target.putResourceResponseType(resourcePath, APPLICATION_JSON);
	}

	@Override
	public void putJsonSchemaResource(final String resourcePath)
	{
		target.putResource(resourcePath, JSON_SCHEMA_MIME);
	}

	@Override
	public ResponseType putJsonSchemaResourceResponseType(final String resourcePath)
	{
		return target.putResourceResponseType(resourcePath, JSON_SCHEMA_MIME);
	}

	@Override
	public void putJsonSchemaString(final String text)
	{
		target.putString(text, JSON_SCHEMA_MIME);
	}

	@Override
	public void putKindString(final String text)
	{
		target.putString(text, APPLICATION_JSON);
	}

	@Override
	public ManagementPathUnknownEntity postNew()
	{
		return new ManagementPathImpl(target.withSegment("new").postAndGetPath("", TEXT_PLAIN));
	}

	@Override
	public ResponseType getResponseTypeForContentType(final String mimeType)
	{
		return target.getResponseType(mimeType);
	}

	@Override
	public ManagementPathEntities entitiesWithKind(final String kindId)
	{
		return new ManagementPathImpl(target.withSegment(ENTITY).withSegment(kindId));
	}

	@Override
	public ManagementPathUnknownEntity entity(final String entityId)
	{
		return new ManagementPathImpl(target.withSegment(entityId));
	}

	@Override
	public ListClassifierResponse listEntityClassifiers()
	{
		return target.withSegment(ENTITY).getJson(ListClassifierResponse.class);
	}

	@Override
	public JsonNode getJsonNode()
	{
		return target.getJson(JsonNode.class);
	}

	@Override
	public void add(final String addName)
	{
		final Map<String, Object> map = ImmutableMap.of("addName", addName);
		target.withSegment("add").postOperationWithJson(map);
	}

	@Override
	public ResponseType addResponseType(final String addName)
	{
		final Map<String, Object> map = ImmutableMap.of("addName", addName);
		return target.withSegment("add").postOperationWithJsonResponseType(map);
	}

	@Override
	public void putString(final String text, final String mimeType)
	{
		target.putString(text, mimeType);
	}

	@Override
	public String getString(final String mimeType)
	{
		return target.getString(mimeType);
	}
}
