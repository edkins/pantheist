package io.pantheist.testclient.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.pantheist.api.flatdir.model.ListFileResponse;
import io.pantheist.api.flatdir.model.ListFlatDirResponse;
import io.pantheist.api.java.model.ApiJavaBinding;
import io.pantheist.api.java.model.ApiJavaFile;
import io.pantheist.api.java.model.ListJavaFileResponse;
import io.pantheist.api.java.model.ListJavaPkgResponse;
import io.pantheist.api.kind.model.ApiKind;
import io.pantheist.api.kind.model.ListEntityResponse;
import io.pantheist.api.kind.model.ListKindResponse;
import io.pantheist.api.management.model.ListConfigItem;
import io.pantheist.api.management.model.ListConfigResponse;
import io.pantheist.api.schema.model.ApiSchema;
import io.pantheist.api.schema.model.ListSchemaResponse;
import io.pantheist.api.sql.model.ListRowResponse;
import io.pantheist.api.sql.model.ListSqlTableResponse;
import io.pantheist.common.api.model.ListClassifierResponse;
import io.pantheist.testclient.api.ManagementData;
import io.pantheist.testclient.api.ManagementFlatDirPath;
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
		ManagementPathSqlRow
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
		return new ManagementPathImpl(target.withSegment(JSON_SCHEMA).withSegment(schemaId));
	}

	@Override
	public ManagementPathKind kind(final String kindId)
	{
		return new ManagementPathImpl(target.withSegment(KIND).withSegment(kindId));
	}

	@Override
	public void putJsonResource(final String resourcePath)
	{
		target.putResource(resourcePath, APPLICATION_JSON);
	}

	@Override
	public ApiKind getKind()
	{
		return target.getJson(ApiKind.class);
	}

	@Override
	public ManagementData data()
	{
		return new ManagementDataImpl(target.withSegment(DATA));
	}

	@Override
	public ResponseType putJsonResourceResponseType(final String resourcePath)
	{
		return target.putResourceResponseType(resourcePath, APPLICATION_JSON);
	}

	@Override
	public ListEntityResponse listEntities()
	{
		return target.withSegment(ENTITY).getJson(ListEntityResponse.class);
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
	public ListKindResponse listKinds()
	{
		return target.withSegment(KIND).getJson(ListKindResponse.class);
	}

	@Override
	public ListSchemaResponse listJsonSchemas()
	{
		return target.withSegment(JSON_SCHEMA).getJson(ListSchemaResponse.class);
	}

	@Override
	public ListJavaFileResponse listJavaFiles()
	{
		return target.withSegment(FILE).getJson(ListJavaFileResponse.class);
	}

	@Override
	public ApiJavaFile describeJavaFile()
	{
		return target.getJson(ApiJavaFile.class);
	}

	@Override
	public ResponseType getJavaFileResponseType()
	{
		return target.getResponseType(APPLICATION_JSON);
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
	public ApiSchema describeSchema()
	{
		return target.getJson(ApiSchema.class);
	}

	@Override
	public ResponseType describeSchemaResponseType()
	{
		return target.getResponseType(APPLICATION_JSON);
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
}
