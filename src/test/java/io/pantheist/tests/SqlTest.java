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

import io.pantheist.api.sql.model.ListSqlTableItem;
import io.pantheist.testclient.api.ManagementPathRoot;
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
}
