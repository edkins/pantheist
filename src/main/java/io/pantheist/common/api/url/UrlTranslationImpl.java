package io.pantheist.common.api.url;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;

import io.pantheist.api.management.backend.UriPattern;
import io.pantheist.api.management.backend.UriPatternImpl;
import io.pantheist.common.api.model.BasicContentType;
import io.pantheist.common.api.model.BindingAction;
import io.pantheist.common.api.model.CommonApiModelFactory;
import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.DataAction;
import io.pantheist.common.api.model.DeleteAction;
import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.common.util.AntiIt;
import io.pantheist.handler.java.model.JavaFileId;
import io.pantheist.handler.java.model.JavaModelFactory;
import io.pantheist.system.config.PantheistConfig;

final class UrlTranslationImpl implements UrlTranslation
{
	private static final String APPLICATION_JSON = "application/json";
	private static final String JSON_SCHEMA_MIME = "application/schema+json";
	private static final String TEXT_PLAIN = "text/plain";
	private final JavaModelFactory javaFactory;
	private final CommonApiModelFactory modelFactory;

	private final UriPattern managementRoot;
	private final UriPattern kind;
	private final UriPattern kindData;
	private final UriPattern kindEntity;
	private final UriPattern jsonSchema;
	private final UriPattern jsonSchemaData;
	private final UriPattern javaBinding;
	private final UriPattern javaPkg;
	private final UriPattern javaFile;
	private final UriPattern javaFileData;
	private final UriPattern location;
	private final UriPattern flatDir;
	private final UriPattern flatDirFile;
	private final UriPattern flatDirFileData;
	private final UriPattern sqlTable;
	private final UriPattern sqlTableColumn;
	private final UriPattern sqlTableColumnRow;
	private final UriPattern sqlTableColumnRowData;
	private final UriPattern clientConfig;

	@Inject
	private UrlTranslationImpl(
			final PantheistConfig config,
			final JavaModelFactory javaFactory,
			final CommonApiModelFactory modelFactory)
	{
		final UriPattern root = UriPatternImpl.hostAndPort("http", "127.0.0.1:" + config.nginxPort())
				.emptySegment();

		this.managementRoot = root;
		this.javaFactory = checkNotNull(javaFactory);
		this.modelFactory = checkNotNull(modelFactory);
		this.kind = root.segment("kind").var("kindId");
		this.kindData = kind.segment("data");
		this.kindEntity = kind.segment("entity").var("entityId");
		this.jsonSchema = root.segment("json-schema").var("schemaId");
		this.jsonSchemaData = jsonSchema.segment("data");
		this.javaBinding = root.segment("java-binding");
		this.javaPkg = root.segment("java-pkg").var("pkg");
		this.javaFile = javaPkg.segment("file").var("file");
		this.javaFileData = javaFile.segment("data");
		this.location = root.segment("server").var("serverId").segment("location").var("locationId");
		this.flatDir = root.segment("flat-dir").var("dir");
		this.flatDirFile = flatDir.segment("file").var("file");
		this.flatDirFileData = flatDirFile.segment("data");
		this.sqlTable = root.segment("sql-table").var("table");
		this.sqlTableColumn = sqlTable.var("column");
		this.sqlTableColumnRow = sqlTableColumn.var("row");
		this.sqlTableColumnRowData = sqlTableColumnRow.segment("data");
		this.clientConfig = root.segment("project").segment("client-config.json");
	}

	@Override
	public String jsonSchemaToUrl(final String jsonSchemaId)
	{
		return jsonSchema.generate(ImmutableMap.of("schemaId", jsonSchemaId));
	}

	@Override
	public String jsonSchemaFromUrl(final String url)
	{
		return jsonSchema.getVar("schemaId", url);
	}

	@Override
	public String javaToUrl(final JavaFileId javaFileId)
	{
		return javaFile.generate(ImmutableMap.of("pkg", javaFileId.pkg(), "file", javaFileId.file()));
	}

	@Override
	public JavaFileId javaFromUrl(@Nullable final String url)
	{
		return javaFactory.fileId(javaFile.getVar("pkg", url), javaFile.getVar("file", url));
	}

	@Override
	public String kindToUrl(final String kindId)
	{
		return kind.generate(ImmutableMap.of("kindId", kindId));
	}

	@Override
	public String kindFromUrl(final String url)
	{
		return kind.getVar("kindId", url);
	}

	@Override
	public String locationToUrl(final String serverId, final String locationId)
	{
		return location.generate(ImmutableMap.of("serverId", serverId, "locationId", locationId));
	}

	private List<ListClassifierItem> classifiers(
			final UriPattern pattern,
			final Map<String, String> values,
			final boolean suggestHidingAll,
			final String... classifierSegments)
	{
		final String classifierKindUrl = kindToUrl("pantheist-classifier");
		return AntiIt.array(classifierSegments)
				.<ListClassifierItem>map(seg -> {
					final String url = pattern.segment(seg).generate(values);
					return modelFactory.listClassifierItem(url, seg, suggestHidingAll, classifierKindUrl);
				})
				.toList();
	}

	@Override
	public List<ListClassifierItem> listRootClassifiers()
	{
		return classifiers(managementRoot, ImmutableMap.of(), false,
				"kind", "java-pkg", "json-schema", "server", "data", "flat-dir", "sql-table");
	}

	@Override
	public String javaPkgToUrl(final String pkg)
	{
		return javaPkg.generate(ImmutableMap.of("pkg", pkg));
	}

	@Override
	public List<ListClassifierItem> listJavaPkgClassifiers(final String pkg)
	{
		return classifiers(javaPkg, ImmutableMap.of("pkg", pkg), true, "file");
	}

	@Override
	public List<ListClassifierItem> listKindClassifiers(final String kindId)
	{
		return classifiers(kind, ImmutableMap.of("kindId", kindId), true, "entity");
	}

	@Override
	public CreateAction javaPkgCreateAction()
	{
		return modelFactory.createAction(BasicContentType.java, TEXT_PLAIN, javaFileData.template(), null);
	}

	@Override
	public DataAction javaFileDataAction(final JavaFileId javaFileId)
	{
		return modelFactory.dataAction(BasicContentType.java, TEXT_PLAIN, true,
				javaFileData.generate(ImmutableMap.of("pkg", javaFileId.pkg(), "file", javaFileId.file())));
	}

	@Override
	public DeleteAction javaFileDeleteAction(final JavaFileId javaFileId)
	{
		return modelFactory.deleteAction();
	}

	@Override
	public CreateAction jsonSchemaCreateAction()
	{
		return modelFactory.createAction(BasicContentType.json, JSON_SCHEMA_MIME, jsonSchemaData.template(), null);
	}

	@Override
	public DataAction jsonSchemaDataAction(final String schemaId)
	{
		return modelFactory.dataAction(BasicContentType.json, JSON_SCHEMA_MIME, true,
				jsonSchemaData.generate(ImmutableMap.of("schemaId", schemaId)));
	}

	@Override
	public DeleteAction jsonSchemaDeleteAction()
	{
		return modelFactory.deleteAction();
	}

	@Override
	public BindingAction javaPkgBindingAction()
	{
		final String url = javaBinding.generate(ImmutableMap.of());
		return modelFactory.bindingAction(url);
	}

	@Override
	public String flatDirFileToUrl(final String dir, final String file)
	{
		return flatDirFile.generate(ImmutableMap.of("dir", dir, "file", file));
	}

	@Override
	public List<ListClassifierItem> listFlatDirClassifiers(final String dir)
	{
		return classifiers(flatDir, ImmutableMap.of("dir", dir), true, "file");
	}

	@Override
	public String flatDirToUrl(final String dir)
	{
		return flatDir.generate(ImmutableMap.of("dir", dir));
	}

	@Override
	public CreateAction kindCreateAction()
	{
		return modelFactory.createAction(BasicContentType.json, APPLICATION_JSON, kind.template(), null);
	}

	@Override
	public String sqlTableToUrl(final String table)
	{
		return sqlTable.generate(ImmutableMap.of("table", table));
	}

	@Override
	public String sqlTableColumnToUrl(final String table, final String column)
	{
		return sqlTableColumn.generate(ImmutableMap.of("table", table, "column", column));
	}

	@Override
	public String sqlRowToUrl(final String table, final String column, final String row)
	{
		return sqlTableColumnRow.generate(ImmutableMap.of("table", table, "column", column, "row", row));
	}

	@Override
	public String entityToUrl(final String kindId, final String entityId)
	{
		return kindEntity.generate(ImmutableMap.of("kindId", kindId, "entityId", entityId));
	}

	@Override
	public DataAction sqlRowDataAction(final String table, final String column, final String row)
	{
		return modelFactory.dataAction(BasicContentType.json, APPLICATION_JSON, false,
				sqlTableColumnRowData.generate(ImmutableMap.of("table", table, "column", column, "row", row)));
	}

	@Override
	public String clientConfigUrl()
	{
		return clientConfig.generate(ImmutableMap.of());
	}

	@Override
	public DataAction flatDirFileDataAction(final String dir, final String file)
	{
		return modelFactory.dataAction(BasicContentType.text, "text/plain", true,
				flatDirFileData.generate(ImmutableMap.of("dir", dir, "file", file)));
	}

	@Override
	public DataAction kindDataAction(final String kindId)
	{
		return modelFactory.dataAction(BasicContentType.json, APPLICATION_JSON, true,
				kindData.generate(ImmutableMap.of("kindId", kindId)));
	}

}
