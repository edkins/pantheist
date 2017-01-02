package io.pantheist.tests;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.Lists;

import io.pantheist.api.sql.model.ListRowItem;
import io.pantheist.api.sql.model.ListSqlTableItem;
import io.pantheist.common.api.model.ListClassifierItem;
import io.pantheist.testclient.api.ManagementPathRoot;
import io.pantheist.testclient.api.ManagementPathSqlTable;
import io.pantheist.testclient.api.ResponseType;
import io.pantheist.testhelpers.classrule.TestSessionImpl;
import io.pantheist.testhelpers.rule.MainRule;

public class SqlTest
{
	@ClassRule
	public static final TestSessionImpl outerRule = TestSessionImpl.forApi();

	@Rule
	public final MainRule mainRule = MainRule.forNewTest(outerRule);

	private ManagementPathRoot manage;

	private static final String JAVA_FILE = "java-file";

	@Before
	public void setup()
	{
		manage = mainRule.actions().manage();
	}

	@Test
	public void javaFile_tableIsListed() throws Exception
	{
		final List<ListSqlTableItem> list = manage.listSqlTables().childResources();

		final List<String> tableNames = Lists.transform(list, ListSqlTableItem::name);
		assertThat(tableNames, hasItem(JAVA_FILE));
	}

	@Test
	public void javaFile_tableExists() throws Exception
	{
		assertThat(manage.sqlTable(JAVA_FILE).listClassifierResponseType(), is(ResponseType.OK));
		assertThat(manage.sqlTable("some-other-garbage").listClassifierResponseType(), is(ResponseType.NOT_FOUND));
	}

	@Test
	public void javaFile_classifiers() throws Exception
	{
		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		final List<? extends ListClassifierItem> list = table.listClassifiers().childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).classifierSegment(), is("qualifiedname"));
		assertThat(list.get(0).url(), is(table.urlOfService("qualifiedname")));
	}

	@Test
	public void javaFile_badTableName_nothingListed() throws Exception
	{
		mainRule.putJavaResource("EmptyClass");

		final ManagementPathSqlTable table1 = manage.sqlTable(JAVA_FILE);
		final ResponseType response1 = table1.listByResponseType("qualifiedname");
		final ManagementPathSqlTable table2 = manage.sqlTable("badtable");
		final ResponseType response2 = table2.listByResponseType("qualifiedname");

		assertThat(response1, is(ResponseType.OK));
		assertThat(response2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void javaFile_badIndexName_nothingListed() throws Exception
	{
		mainRule.putJavaResource("EmptyClass");

		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		final ResponseType response1 = table.listByResponseType("qualifiedname");
		final ResponseType response2 = table.listByResponseType("sjgkrjgahrklga");

		assertThat(response1, is(ResponseType.OK));
		assertThat(response2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void javaFile_create_listedInTable() throws Exception
	{
		mainRule.putJavaResource("EmptyClass");

		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		final List<ListRowItem> list = table.listBy("qualifiedname").childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(table.row("qualifiedname", "io.pantheist.examples.EmptyClass").url()));
		assertThat(list.get(0).kindUrl(), is(manage.kind("sql-row").url()));
	}

	@Test
	public void javaFile_regenerateDb_stillListed() throws Exception
	{
		mainRule.putJavaResource("EmptyClass");

		mainRule.actions().regenerateDb();

		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		final List<ListRowItem> list = table.listBy("qualifiedname").childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(table.row("qualifiedname", "io.pantheist.examples.EmptyClass").url()));
	}
}
