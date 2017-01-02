package io.pantheist.tests;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import io.pantheist.api.sql.model.ListRowItem;
import io.pantheist.api.sql.model.ListSqlTableItem;
import io.pantheist.common.api.model.BasicContentType;
import io.pantheist.common.api.model.DataAction;
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

	private ObjectMapper objectMapper;

	private static final String JAVA_FILE = "java-file";
	private static final String APPLICATION_JSON = "application/json";
	private static final String SIMPLE_NAME = "EmptyClass";
	private static final String QNAME = "io.pantheist.examples.EmptyClass";
	private static final String JAVATHING_SNAME = "JavaThing";
	private static final String JAVATHING_QNAME = "io.pantheist.examples.JavaThing";
	private static final String QUALIFIED_NAME = "qualifiedName";
	private static final String IS_INTERFACE = "isInterface";
	private static final String IS_CLASS = "isClass";

	@Before
	public void setup()
	{
		manage = mainRule.actions().manage();
		objectMapper = new ObjectMapper();
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
		assertThat(list.get(0).classifierSegment(), is(QUALIFIED_NAME));
		assertThat(list.get(0).url(), is(table.urlOfService(QUALIFIED_NAME)));
	}

	@Test
	public void javaFile_badTableName_nothingListed() throws Exception
	{
		mainRule.putJavaResource(SIMPLE_NAME);

		final ManagementPathSqlTable table1 = manage.sqlTable(JAVA_FILE);
		final ResponseType response1 = table1.listByResponseType(QUALIFIED_NAME);
		final ManagementPathSqlTable table2 = manage.sqlTable("badtable");
		final ResponseType response2 = table2.listByResponseType(QUALIFIED_NAME);

		assertThat(response1, is(ResponseType.OK));
		assertThat(response2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void javaFile_badIndexName_nothingListed() throws Exception
	{
		mainRule.putJavaResource(SIMPLE_NAME);

		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		final ResponseType response1 = table.listByResponseType(QUALIFIED_NAME);
		final ResponseType response2 = table.listByResponseType("sjgkrjgahrklga");

		assertThat(response1, is(ResponseType.OK));
		assertThat(response2, is(ResponseType.NOT_FOUND));
	}

	@Test
	public void javaFile_create_listedInTable() throws Exception
	{
		mainRule.putJavaResource(SIMPLE_NAME);

		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		final List<ListRowItem> list = table.listBy(QUALIFIED_NAME).childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(table.row(QUALIFIED_NAME, QNAME).url()));
		assertThat(list.get(0).kindUrl(), is(manage.kind("sql-row").url()));
	}

	@Test
	public void javaFile_regenerateDb_stillListed() throws Exception
	{
		mainRule.putJavaResource(SIMPLE_NAME);

		mainRule.actions().regenerateDb();

		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		final List<ListRowItem> list = table.listBy(QUALIFIED_NAME).childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(table.row(QUALIFIED_NAME, QNAME).url()));
	}

	@Test
	public void javaFile_create_canSeeItem() throws Exception
	{
		mainRule.putJavaResource(SIMPLE_NAME);

		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		final ResponseType response1 = table.row(QUALIFIED_NAME, QNAME)
				.getSqlRowResponseType();
		final ResponseType response2 = table.row(QUALIFIED_NAME, "io.pantheist.examples.BadClass")
				.getSqlRowResponseType();
		final ResponseType response3 = table.row("badcolumn", QNAME)
				.getSqlRowResponseType();

		assertThat(response1, is(ResponseType.OK));
		assertThat(response2, is(ResponseType.NOT_FOUND));
		assertThat(response3, is(ResponseType.NOT_FOUND));

		final DataAction dataAction = table.row(QUALIFIED_NAME, QNAME).getSqlRow()
				.dataAction();
		assertThat(dataAction.basicType(), is(BasicContentType.json));
		assertThat(dataAction.mimeType(), is(APPLICATION_JSON));
		assertFalse("SQL table does not support putting", dataAction.canPut());
	}

	@Test
	public void javaFile_create_canGetData() throws Exception
	{
		mainRule.putJavaResource(SIMPLE_NAME);

		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		final String json = table.row(QUALIFIED_NAME, QNAME).data().getString(APPLICATION_JSON);

		final Map<?, ?> map = objectMapper.readValue(json, Map.class);
		assertThat(map.get(QUALIFIED_NAME), is(QNAME));
		assertThat(map.get(IS_CLASS), is(true));
		assertThat(map.get(IS_INTERFACE), is(false));
	}

	@Test
	public void javaFile_update_canGetNewData() throws Exception
	{
		final ManagementPathSqlTable table = manage.sqlTable(JAVA_FILE);
		mainRule.putJavaResource(JAVATHING_SNAME, "JavaThing-interface");

		final String json1 = table.row(QUALIFIED_NAME, JAVATHING_QNAME).data().getString(APPLICATION_JSON);
		assertThat(objectMapper.readValue(json1, Map.class).get(IS_INTERFACE), is(true));

		mainRule.putJavaResource(JAVATHING_SNAME, "JavaThing-class");
		final String json2 = table.row(QUALIFIED_NAME, JAVATHING_QNAME).data().getString(APPLICATION_JSON);
		assertThat(objectMapper.readValue(json2, Map.class).get(IS_INTERFACE), is(false));
	}
}
