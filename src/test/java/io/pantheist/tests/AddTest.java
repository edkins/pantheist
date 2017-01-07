package io.pantheist.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.pantheist.api.entity.model.ListEntityItem;
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

	@Test
	public void fileBasedKind_invalidEntityCanBePut() throws Exception
	{
		final ManagementPathKind kind = mainRule.putKindResourceWithPort("file-json-with-array");
		mainRule.putJsonSchemaResourceWithPort("file-json-with-array");

		final ManagementPathUnknownEntity entity = kind.postNew();

		entity.putString("{some invalid json}", APPLICATION_JSON);
		assertThat(entity.getString(APPLICATION_JSON), is("{some invalid json}"));
	}

	@Test
	public void fileBasedKind_entityCanBeDeleted() throws Exception
	{
		final ManagementPathKind kind = mainRule.putKindResourceWithPort("file-json-with-array");
		mainRule.putJsonSchemaResourceWithPort("file-json-with-array");

		final ManagementPathUnknownEntity entity = kind.postNew();

		assertThat(entity.getResponseTypeForContentType(APPLICATION_JSON), is(ResponseType.OK));

		entity.delete();

		assertThat(entity.getResponseTypeForContentType(APPLICATION_JSON), is(ResponseType.NOT_FOUND));
	}

	@Test
	public void fileBasedKind_blankEntityIsCorrect() throws Exception
	{
		final ManagementPathKind kind = mainRule.putKindResourceWithPort("file-json-with-array");
		mainRule.putJsonSchemaResourceWithPort("file-json-with-array");

		final ManagementPathUnknownEntity entity = kind.postNew();

		final JsonNode stuff = entity.getJsonNode().get("stuff");
		assertTrue("stuff should be array", stuff.isArray());
		assertThat(stuff.size(), is(0));
	}

	@Test
	public void fileBasedKind_addArrayItem() throws Exception
	{
		final ManagementPathKind kind = mainRule.putKindResourceWithPort("file-json-with-array");
		mainRule.putJsonSchemaResourceWithPort("file-json-with-array");

		final ManagementPathUnknownEntity entity = kind.postNew();

		entity.add("Add stuff");

		final JsonNode stuff = entity.getJsonNode().get("stuff");
		assertThat(stuff.size(), is(1));
		assertThat(stuff.get(0).textValue(), is("This is the new item."));
	}
}
