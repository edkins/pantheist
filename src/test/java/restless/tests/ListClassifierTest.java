package restless.tests;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import restless.api.java.model.ListJavaPkgItem;
import restless.client.api.ManagementPathEntity;
import restless.client.api.ManagementPathJavaFile;
import restless.client.api.ManagementPathJavaPackage;
import restless.client.api.ManagementPathKind;
import restless.client.api.ManagementPathSchema;
import restless.client.api.ResponseType;
import restless.common.api.model.AdditionalStructureItem;
import restless.common.api.model.BasicContentType;
import restless.common.api.model.CreateAction;
import restless.common.api.model.DataAction;
import restless.common.api.model.ListClassifierItem;

public class ListClassifierTest extends BaseTest
{
	private static final String JAVA_PKG = "restless.examples";
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

		assertThat(segs, hasItems("data", "server", "java-pkg", "json-schema", "entity", "kind"));
		assertThat(urls, hasItem(manage.urlOfService("java-pkg")));
	}

	@Test
	public void nonexistentEntity_noClassifiers() throws Exception
	{
		manage.entity("exists").putEntity(null, null, null);

		final ResponseType response1 = manage.entity("exists").listClassifierResponseType();
		final ResponseType response2 = manage.entity("does-not-exist").listClassifierResponseType();

		assertThat(response1, is(ResponseType.OK));
		assertThat(response2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void entity_classifiers() throws Exception
	{
		final ManagementPathEntity entity = manage.entity("my-entity");
		entity.putEntity(null, null, null);

		final List<? extends ListClassifierItem> list = entity.listClassifiers().childResources();

		final List<String> urls = Lists.transform(list, ListClassifierItem::url);
		final List<String> segs = Lists.transform(list, ListClassifierItem::classifierSegment);

		assertThat(urls, containsInAnyOrder(entity.urlOfService("component")));
		assertThat(segs, containsInAnyOrder("component"));
	}

	@Test
	public void javaPkg_listed() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		pkg.file(JAVA_EMPTY_CLASS_NAME).data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

		final List<ListJavaPkgItem> list = manage.listJavaPackages().childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(pkg.url()));
	}

	@Test
	public void javaPkg_classifiers() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		pkg.file(JAVA_EMPTY_CLASS_NAME).data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

		final List<? extends ListClassifierItem> list = pkg.listClassifiers().childResources();

		final List<String> urls = Lists.transform(list, ListClassifierItem::url);
		final List<String> segs = Lists.transform(list, ListClassifierItem::classifierSegment);

		assertThat(urls, containsInAnyOrder(pkg.urlOfService("file")));
		assertThat(segs, containsInAnyOrder("file"));
	}

	@Test
	public void javaPkg_thatDoesNotExist_noClassifiers() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaPackage bad = manage.javaPackage("some.invalid.package");
		pkg.file(JAVA_EMPTY_CLASS_NAME).data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

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
		assertThat(createAction.mimeType(), is("text/plain"));

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
		file.data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

		final DataAction dataAction = file.describeJavaFile().dataAction();
		assertThat(dataAction.basicType(), is(BasicContentType.java));
		assertThat(dataAction.mimeType(), is("text/plain"));
		assertTrue("Should say we can put", dataAction.canPut());
	}

	@Test
	public void javaFile_deleteAction() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile file = pkg.file(JAVA_EMPTY_CLASS_NAME);
		file.data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

		assertThat(file.describeJavaFile().deleteAction(), notNullValue());
	}

	@Test
	public void javaFile_notThere_noDataAction() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile file = pkg.file(JAVA_EMPTY_CLASS_NAME);
		final ManagementPathJavaFile bad = pkg.file("FileThatIsNotThere");
		file.data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

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
}
