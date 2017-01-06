package io.pantheist.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import io.pantheist.api.kind.model.ListEntityItem;
import io.pantheist.testclient.api.ManagementPathEntities;
import io.pantheist.testclient.api.ManagementPathKind;
import io.pantheist.testclient.api.ManagementPathRoot;
import io.pantheist.testclient.api.ManagementPathUnknownEntity;
import io.pantheist.testclient.api.ResponseType;
import io.pantheist.testhelpers.classrule.TestSessionImpl;
import io.pantheist.testhelpers.rule.MainRule;

public class AddTest
{
	private static final String APPLICATION_JSON = "application/json";

	@ClassRule
	public static final TestSessionImpl outerRule = TestSessionImpl.forApi();

	@Rule
	public final MainRule mainRule = MainRule.forNewTest(outerRule);

	private ManagementPathRoot manage;

	@Before
	public void setup()
	{
		manage = mainRule.actions().manage();
	}

	@Test
	public void fileBasedKind_entityIsListed() throws Exception
	{
		final ManagementPathKind kind = mainRule.putKindResourceWithPort("file-json-with-array");
		mainRule.putJsonSchemaResourceWithPort("file-json-with-array");

		final ManagementPathUnknownEntity entity = kind.postNew();

		final ManagementPathEntities entities = manage.entitiesWithKind("file-json-with-array");
		final List<ListEntityItem> list = entities
				.listEntities()
				.childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(entity.url()));
		assertThat(list.get(0).url(), is(entities.entity("new1").url()));
	}

	@Test
	public void fileBasedKind_entityExists() throws Exception
	{
		final ManagementPathKind kind = mainRule.putKindResourceWithPort("file-json-with-array");
		mainRule.putJsonSchemaResourceWithPort("file-json-with-array");

		final ManagementPathUnknownEntity entity = kind.postNew();

		assertThat(entity.getResponseTypeForContentType(APPLICATION_JSON), is(ResponseType.OK));
	}
}
