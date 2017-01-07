package io.pantheist.tests;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;

import io.pantheist.api.java.model.ListJavaPkgItem;
import io.pantheist.common.api.model.BindingAction;
import io.pantheist.common.api.model.CreateAction;
import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.testclient.api.ManagementFlatDirPath;
import io.pantheist.testclient.api.ManagementPathJavaFile;
import io.pantheist.testclient.api.ManagementPathJavaPackage;
import io.pantheist.testclient.api.ManagementPathRoot;
import io.pantheist.testclient.api.ResponseType;
import io.pantheist.testhelpers.classrule.TestSessionImpl;
import io.pantheist.testhelpers.rule.MainRule;

public class ListClassifierTest
{
	@ClassRule
	public static final TestSessionImpl outerRule = TestSessionImpl.forApi();

	@Rule
	public final MainRule mainRule = MainRule.forNewTest(outerRule);

	private ManagementPathRoot manage;

	private static final String JAVA_PKG = "io.pantheist.examples";
	private static final String JAVA_EMPTY_CLASS_RES = "/java-example/EmptyClass";
	private static final String JAVA_EMPTY_CLASS_NAME = "EmptyClass";

	@Before
	public void setup()
	{
		manage = mainRule.actions().manage();
	}

	@Test
	public void root_classifiers() throws Exception
	{
		final List<? extends ListClassifierItem> list = manage.listClassifiers().childResources();

		final List<String> urls = Lists.transform(list, ListClassifierItem::url);
		final List<String> segs = Lists.transform(list, ListClassifierItem::classifierSegment);

		assertThat(segs,
				containsInAnyOrder("data", "server", "java-pkg", "flat-dir", "sql-table",
						"entity"));
		assertThat(urls, hasItem(manage.urlOfService("java-pkg")));
	}

	@Test
	public void entity_classifiers() throws Exception
	{
		final List<? extends ListClassifierItem> list = manage.listEntityClassifiers().childResources();

		final List<String> urls = Lists.transform(list, ListClassifierItem::url);
		final List<String> segs = Lists.transform(list, ListClassifierItem::classifierSegment);

		assertThat(segs, containsInAnyOrder("java-file", "json-schema", "kind"));
		assertThat(urls, hasItem(manage.entitiesWithKind("java-file").url()));
	}

	@Test
	public void entity_classifiers_custom_file() throws Exception
	{
		mainRule.putKindResourceWithPort("file-json-with-array");
		mainRule.putJsonSchemaResourceWithPort("file-json-with-array");

		final List<? extends ListClassifierItem> list = manage.listEntityClassifiers().childResources();
		final List<String> segs = Lists.transform(list, ListClassifierItem::classifierSegment);
		assertThat(segs, containsInAnyOrder("java-file", "json-schema", "file-json-with-array", "kind"));
	}

	@Test
	public void entity_classifiers_custom_javaFile() throws Exception
	{
		mainRule.putKindResource("java-interface-sugar");

		final List<? extends ListClassifierItem> list = manage.listEntityClassifiers().childResources();
		final List<String> segs = Lists.transform(list, ListClassifierItem::classifierSegment);
		assertThat(segs, containsInAnyOrder("java-file", "json-schema", "kind", "java-interface-sugar"));
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
		pkg.file(JAVA_EMPTY_CLASS_NAME).putJavaResource(JAVA_EMPTY_CLASS_RES);

		final List<ListJavaPkgItem> list = manage.listJavaPackages().childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(pkg.url()));
	}

	@Test
	public void javaPkg_classifiers() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		pkg.file(JAVA_EMPTY_CLASS_NAME).putJavaResource(JAVA_EMPTY_CLASS_RES);

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
		pkg.file(JAVA_EMPTY_CLASS_NAME).putJavaResource(JAVA_EMPTY_CLASS_RES);

		final ResponseType response1 = pkg.listClassifierResponseType();
		final ResponseType response2 = bad.listClassifierResponseType();

		assertThat(response1, is(ResponseType.OK));
		assertThat(response2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void javaPkg_createAction() throws Exception
	{
		final CreateAction createAction = manage.listJavaPackages().createAction();

		assertThat(createAction.urlTemplate(), containsString("java-pkg/{pkg}/file/{file}"));
	}

	@Test
	public void javaFile_notThere_noDataAction() throws Exception
	{
		final ManagementPathJavaPackage pkg = manage.javaPackage(JAVA_PKG);
		final ManagementPathJavaFile file = pkg.file(JAVA_EMPTY_CLASS_NAME);
		final ManagementPathJavaFile bad = pkg.file("FileThatIsNotThere");
		file.putJavaResource(JAVA_EMPTY_CLASS_RES);

		final ResponseType response1 = file.getJavaFileResponseType();
		final ResponseType response2 = bad.getJavaFileResponseType();
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
}
