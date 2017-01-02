package io.pantheist.tests;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import io.pantheist.api.java.model.ListJavaPkgItem;
import io.pantheist.common.api.model.AdditionalStructureItem;
import io.pantheist.common.api.model.BasicContentType;
import io.pantheist.common.api.model.BindingAction;
import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.DataAction;
import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.common.api.model.ReplaceAction;
import io.pantheist.testclient.api.ManagementFlatDirPath;
import io.pantheist.testclient.api.ManagementPathJavaFile;
import io.pantheist.testclient.api.ManagementPathJavaPackage;
import io.pantheist.testclient.api.ManagementPathKind;
import io.pantheist.testclient.api.ManagementPathSchema;
import io.pantheist.testclient.api.ManagementPathSqlTable;
import io.pantheist.testclient.api.ResponseType;

public class ListClassifierTest extends BaseTest
{
	private static final String APPLICATION_JSON = "application/json";
	private static final String TEXT_PLAIN = "text/plain";
	private static final String JAVA_PKG = "io.pantheist.examples";
	private static final String JAVA_EMPTY_CLASS_RES = "/java-example/EmptyClass";
	private static final String JAVA_EMPTY_CLASS_NAME = "EmptyClass";
	private static final String KIND_SCHEMA_RES = "/kind-schema/kind-test-example";
	private static final String JSON_SCHEMA_MIME = "application/schema+json";
	private static final String JSON_SCHEMA_COFFEE_RES = "/json-schema/coffee";

	@Test
	public void root_classifiers() throws Exception
	{
		final List<? extends ListClassifierItem> list = manage.listClassifiers().childResources();

		final List<String> urls = Lists.transform(list, ListClassifierItem::url);
		final List<String> segs = Lists.transform(list, ListClassifierItem::classifierSegment);

		assertThat(segs,
				containsInAnyOrder("data", "server", "java-pkg", "json-schema", "kind", "flat-dir", "sql-table"));
		assertThat(urls, hasItem(manage.urlOfService("java-pkg")));
	}

	@Test
	public void root_classifier_kind() throws Exception
	{
		final List<? extends ListClassifierItem> list = manage.listClassifiers().childResources();

		assertThat(list.get(0).kindUrl(), is(manage.kind("pantheist-classifier").url()));
	}

	@Test
	public void javaPkg_listed() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		pkg.file(JAVA_EMPTY_CLASS_NAME).data().putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		final List<ListJavaPkgItem> list = manage.listJavaPackages().childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(pkg.url()));
	}

	@Test
	public void javaPkg_classifiers() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		pkg.file(JAVA_EMPTY_CLASS_NAME).data().putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		final List<? extends ListClassifierItem> list = pkg.listClassifiers().childResources();

		final List<String> urls = Lists.transform(list, ListClassifierItem::url);
		final List<String> segs = Lists.transform(list, ListClassifierItem::classifierSegment);

		assertThat(urls, containsInAnyOrder(pkg.urlOfService("file")));
		assertThat(segs, containsInAnyOrder("file"));
		assertThat(list.get(0).kindUrl(), is(manage.kind("pantheist-classifier").url()));
	}

	@Test
	public void javaPkg_thatDoesNotExist_noClassifiers() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaPackage bad = manage.javaPackage("some.invalid.package");
		pkg.file(JAVA_EMPTY_CLASS_NAME).data().putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		final ResponseType response1 = pkg.listClassifierResponseType();
		final ResponseType response2 = bad.listClassifierResponseType();

		assertThat(response1, is(ResponseType.OK));
		assertThat(response2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void kind_classifiers() throws Exception
	{
		final ManagementPathKind kind = manage.kind("my-kind");
		kind.putJsonResource(KIND_SCHEMA_RES);

		final List<? extends ListClassifierItem> list = kind.listClassifiers().childResources();

		final List<String> urls = Lists.transform(list, ListClassifierItem::url);
		final List<String> segs = Lists.transform(list, ListClassifierItem::classifierSegment);

		assertThat(urls, containsInAnyOrder(kind.urlOfService("entity")));
		assertThat(segs, containsInAnyOrder("entity"));
	}

	@Test
	public void javaPkg_createAction() throws Exception
	{
		final CreateAction createAction = manage.listJavaPackages().createAction();
		final List<AdditionalStructureItem> additional = createAction.additionalStructure();

		assertThat(createAction.basicType(), is(BasicContentType.java));
		assertThat(createAction.mimeType(), is(TEXT_PLAIN));

		assertThat(additional.size(), is(3));
		assertTrue("First segment should be literal", additional.get(0).literal());
		assertThat(additional.get(0).name(), is("file"));

		assertFalse("Second segment should be var", additional.get(1).literal());
		// the actual name of the var doesn't really matter
		assertThat(additional.get(1).name(), not(isEmptyOrNullString()));

		assertTrue("Third segment should be literal", additional.get(2).literal());
		assertThat(additional.get(2).name(), is("data"));
	}

	@Test
	public void javaFile_dataAction() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile file = pkg.file(JAVA_EMPTY_CLASS_NAME);
		file.data().putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		final DataAction dataAction = file.describeJavaFile().dataAction();
		assertThat(dataAction.basicType(), is(BasicContentType.java));
		assertThat(dataAction.mimeType(), is(TEXT_PLAIN));
		assertTrue("Should say we can put", dataAction.canPut());
	}

	@Test
	public void javaFile_deleteAction() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile file = pkg.file(JAVA_EMPTY_CLASS_NAME);
		file.data().putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		assertThat(file.describeJavaFile().deleteAction(), notNullValue());
	}

	@Test
	public void javaFile_notThere_noDataAction() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile file = pkg.file(JAVA_EMPTY_CLASS_NAME);
		final ManagementPathJavaFile bad = pkg.file("FileThatIsNotThere");
		file.data().putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		final ResponseType response1 = file.getJavaFileResponseType();
		final ResponseType response2 = bad.getJavaFileResponseType();
		assertThat(response1, is(ResponseType.OK));
		assertThat(response2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void jsonSchema_createAction() throws Exception
	{
		final CreateAction createAction = manage.listJsonSchemas().createAction();

		assertThat(createAction.basicType(), is(BasicContentType.json));
		assertThat(createAction.mimeType(), is(JSON_SCHEMA_MIME));
		assertThat(createAction.additionalStructure().size(), is(1));
		assertThat(createAction.additionalStructure().get(0).literal(), is(true));
		assertThat(createAction.additionalStructure().get(0).name(), is("data"));
	}

	@Test
	public void jsonSchema_dataAction() throws Exception
	{
		final ManagementPathSchema schema = mainRule.actions().manage().jsonSchema("coffee");
		schema.data().putResource(JSON_SCHEMA_COFFEE_RES, JSON_SCHEMA_MIME);

		final DataAction dataAction = schema.describeSchema().dataAction();

		assertThat(dataAction.basicType(), is(BasicContentType.json));
		assertThat(dataAction.mimeType(), is(JSON_SCHEMA_MIME));
		assertThat(dataAction.canPut(), is(true));
	}

	@Test
	public void jsonSchema_deleteAction() throws Exception
	{
		final ManagementPathSchema schema = mainRule.actions().manage().jsonSchema("coffee");
		schema.data().putResource(JSON_SCHEMA_COFFEE_RES, JSON_SCHEMA_MIME);

		assertThat(schema.describeSchema().deleteAction(), notNullValue());
	}

	@Test
	public void jsonSchema_ifMissing_noDataAction() throws Exception
	{
		final ManagementPathSchema schema = mainRule.actions().manage().jsonSchema("coffee");
		final ManagementPathSchema bad = mainRule.actions().manage().jsonSchema("mud");
		schema.data().putResource(JSON_SCHEMA_COFFEE_RES, JSON_SCHEMA_MIME);

		final ResponseType response1 = schema.describeSchemaResponseType();
		final ResponseType response2 = bad.describeSchemaResponseType();
		assertThat(response1, is(ResponseType.OK));
		assertThat(response2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void javaPkg_bindingAction() throws Exception
	{
		final BindingAction bindingAction = manage.listJavaPackages().bindingAction();
		assertThat(bindingAction, notNullValue());
		assertThat(bindingAction.url(), is(manage.urlOfService("java-binding")));
	}

	@Test
	public void flatDir_classifiers() throws Exception
	{
		final ManagementFlatDirPath flatDir = manage.flatDir("srv/resources");
		final List<? extends ListClassifierItem> list = flatDir
				.listClassifiers()
				.childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).classifierSegment(), is("file"));
		assertThat(list.get(0).url(), is(flatDir.urlOfService("file")));
	}

	@Test
	public void flatDir_thatDoesNotExist_noClassifiers() throws Exception
	{
		final ManagementFlatDirPath flatDir = manage.flatDir("srv/resources");
		final ManagementFlatDirPath badDir = manage.flatDir("srv/xxx");

		assertThat(flatDir.listClassifierResponseType(), is(ResponseType.OK));
		assertThat(badDir.listClassifierResponseType(), is(ResponseType.NOT_FOUND));
	}

	@Test
	public void kind_createAction() throws Exception
	{
		final CreateAction createAction = manage.listKinds().createAction();

		assertThat(createAction, notNullValue());
		assertThat(createAction.basicType(), is(BasicContentType.json));
		assertThat(createAction.mimeType(), is(APPLICATION_JSON));
		assertThat(createAction.additionalStructure(), nullValue());
	}

	@Test
	public void kind_replaceAction() throws Exception
	{
		final ManagementPathKind kind = manage.kind("my-kind");
		kind.putJsonResource(KIND_SCHEMA_RES);

		final ReplaceAction replaceAction = kind.getKind().replaceAction();

		assertThat(replaceAction, notNullValue());
		assertThat(replaceAction.basicType(), is(BasicContentType.json));
		assertThat(replaceAction.mimeType(), is(APPLICATION_JSON));
	}

	@Test
	public void sqlTable_classifiers() throws Exception
	{
		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		final List<? extends ListClassifierItem> list = table.listClassifiers().childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).classifierSegment(), is("row"));
		assertThat(list.get(0).url(), is(table.urlOfService("row")));
	}
}
