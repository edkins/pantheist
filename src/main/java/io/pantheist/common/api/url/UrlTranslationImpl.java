package io.pantheist.common.api.url;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.pantheist.api.management.backend.UriPattern;
import io.pantheist.api.management.backend.UriPatternImpl;
import io.pantheist.common.api.model.AdditionalStructureItem;
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
	private static final String JSON_SCHEMA_MIME = "application/schema+json";
	private static final String TEXT_PLAIN = "text/plain";
	private final JavaModelFactory javaFactory;
	private final CommonApiModelFactory modelFactory;

	private final UriPattern managementRoot;
	private final UriPattern kind;
	private final UriPattern jsonSchema;
	private final UriPattern javaBinding;
	private final UriPattern javaPkg;
	private final UriPattern javaFile;
	private final UriPattern location;
	private final UriPattern entity;
	private final UriPattern component;
	private final UriPattern flatDir;
	private final UriPattern flatDirFile;

	@Inject
	private UrlTranslationImpl(
			final PantheistConfig config,
			final JavaModelFactory javaFactory,
			final CommonApiModelFactory modelFactory)
	{
		final UriPattern root = UriPatternImpl.hostAndPort("http", "127.0.0.1:" + config.mainPort())
				.emptySegment();

		this.managementRoot = root;
		this.javaFactory = checkNotNull(javaFactory);
		this.modelFactory = checkNotNull(modelFactory);
		this.entity = root.segment("entity").var("entityId");
		this.component = entity.segment("component").var("componentId");
		this.kind = root.segment("kind").var("kindId");
		this.jsonSchema = root.segment("json-schema").var("schemaId");
		this.javaBinding = root.segment("java-binding");
		this.javaPkg = root.segment("java-pkg").var("pkg");
		this.javaFile = javaPkg.segment("file").var("file");
		this.location = root.segment("server").var("serverId").segment("location").var("locationId");
		this.flatDir = root.segment("flat-dir").var("dir");
		this.flatDirFile = flatDir.segment("file").var("file");
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
		return AntiIt.array(classifierSegments)
				.<ListClassifierItem>map(seg -> {
					final String url = pattern.segment(seg).generate(values);
					return modelFactory.listClassifierItem(url, seg, suggestHidingAll);
				})
				.toList();
	}

	@Override
	public List<ListClassifierItem> listRootClassifiers()
	{
		return classifiers(managementRoot, ImmutableMap.of(), false,
				"entity", "kind", "java-pkg", "json-schema", "server", "data", "flat-dir");
	}

	@Override
	public List<ListClassifierItem> listEntityClassifiers(final String entityId)
	{
		return classifiers(entity, ImmutableMap.of("entityId", entityId), true,
				"component");
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
	public String entityToUrl(final String entityId)
	{
		return entity.generate(ImmutableMap.of("entityId", entityId));
	}

	@Override
	public String componentToUrl(final String entityId, final String componentId)
	{
		return component.generate(ImmutableMap.of("entityId", entityId, "componentId", componentId));
	}

	@Override
	public CreateAction javaPkgCreateAction()
	{
		// This looks mysterious, but what it's saying is:
		// - the client already expects urls to look like "/java-pkg/{}"
		// - we are telling it that additional segments need to be appended to create resources in there
		// - and the additional structure is "file/{}"

		final ImmutableList<AdditionalStructureItem> additionalStructure = ImmutableList.of(
				modelFactory.additionalStructureItem(true, "file", false),
				modelFactory.additionalStructureItem(false, "file", false),
				modelFactory.additionalStructureItem(true, "data", true));
		return modelFactory.createAction(BasicContentType.java, TEXT_PLAIN, additionalStructure);
	}

	@Override
	public DataAction javaFileDataAction(final JavaFileId javaFileId)
	{
		return modelFactory.dataAction(BasicContentType.java, TEXT_PLAIN, true);
	}

	@Override
	public DeleteAction javaFileDeleteAction(final JavaFileId javaFileId)
	{
		return modelFactory.deleteAction();
	}

	@Override
	public CreateAction jsonSchemaCreateAction()
	{
		final ImmutableList<AdditionalStructureItem> additionalStructure = ImmutableList.of(
				modelFactory.additionalStructureItem(true, "data", true));
		return modelFactory.createAction(BasicContentType.json, JSON_SCHEMA_MIME, additionalStructure);
	}

	@Override
	public DataAction jsonSchemaDataAction()
	{
		return modelFactory.dataAction(BasicContentType.json, JSON_SCHEMA_MIME, true);
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

}
