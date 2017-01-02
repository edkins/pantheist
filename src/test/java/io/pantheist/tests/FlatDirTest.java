package io.pantheist.tests;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;

import io.pantheist.api.flatdir.model.ListFileItem;
import io.pantheist.api.flatdir.model.ListFlatDirItem;
import io.pantheist.testclient.api.ManagementPathRoot;
import io.pantheist.testhelpers.classrule.TestSessionImpl;
import io.pantheist.testhelpers.rule.MainRule;

public class FlatDirTest
{
	@ClassRule
	public static final TestSessionImpl outerRule = TestSessionImpl.forApi();

	@Rule
	public final MainRule mainRule = MainRule.forNewTest(outerRule);

	private ManagementPathRoot manage;

	private static final String TEXT_PLAIN = "text/plain";
	private static final String JAVA_PKG = "io.pantheist.examples";
	private static final String JAVA_EMPTY_CLASS_RES = "/java-example/EmptyClass";
	private static final String JAVA_EMPTY_CLASS_NAME = "EmptyClass";

	@Before
	public void setup()
	{
		manage = mainRule.actions().manage();
	}

	@Test
	public void flatDir_singleSegment_canList() throws Exception
	{
		// Create some java to make sure it has java-binding
		manage.javaPackage(JAVA_PKG)
				.file(JAVA_EMPTY_CLASS_NAME)
				.data()
				.putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		final List<ListFileItem> list = manage.flatDir("system").listFlatDirFiles().childResources();

		final List<String> fileNames = Lists.transform(list, ListFileItem::fileName);

		// A few list of things we expect to find in there. Not intended to be a complete list.
		assertThat(fileNames, hasItems("nginx.conf", "java-binding"));

		// Should not list subdirectories here.
		assertThat(fileNames, not(hasItem("java")));

		// Should not list special items . and ..
		assertThat(fileNames, not(hasItem(".")));
		assertThat(fileNames, not(hasItem("..")));
	}

	@Test
	public void flatDir_mutliSegment_canList() throws Exception
	{
		manage.javaPackage(JAVA_PKG)
				.file(JAVA_EMPTY_CLASS_NAME)
				.data()
				.putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		final List<ListFileItem> list = manage.flatDir("system/java/io/pantheist/examples")
				.listFlatDirFiles()
				.childResources();

		final List<String> fileNames = Lists.transform(list, ListFileItem::fileName);

		assertThat(fileNames, contains("EmptyClass.java"));
	}

	@Test
	public void flatDir_root_canList() throws Exception
	{
		manage.javaPackage(JAVA_PKG)
				.file(JAVA_EMPTY_CLASS_NAME)
				.data()
				.putResource(JAVA_EMPTY_CLASS_RES, TEXT_PLAIN);

		final List<ListFileItem> list = manage.flatDir("/")
				.listFlatDirFiles()
				.childResources();

		final List<String> fileNames = Lists.transform(list, ListFileItem::fileName);

		// This is where the tests happen to put their pantheist.conf file.
		assertThat(fileNames, contains("pantheist.conf"));
	}

	@Test
	public void listFlatDirs() throws Exception
	{
		final List<ListFlatDirItem> list = manage.listFlatDirs().childResources();

		final List<String> dirs = Lists.transform(list, ListFlatDirItem::relativePath);

		assertThat(dirs, hasItems("/", "system", "srv", "srv/resources"));
	}
}
