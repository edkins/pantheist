package restless.tests;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import restless.api.entity.model.ApiComponent;
import restless.api.entity.model.ApiEntity;
import restless.api.entity.model.ListComponentItem;
import restless.api.entity.model.ListEntityItem;
import restless.client.api.ManagementPathEntity;
import restless.client.api.ManagementPathJavaFile;
import restless.client.api.ManagementPathSchema;
import restless.client.api.ResponseType;

public class EntityTest extends BaseTest
{
	private static final String ENTITY_ID = "my-entity";
	private static final String ROOT = ".";
	private ManagementPathSchema schema;
	private ManagementPathJavaFile java;

	private void entitySetup()
	{
		schema = mainRule.actions().manage().jsonSchema("my_schema");
		schema.data().putResource("/json-schema/nonempty_nonnegative_int_list", "application/schema+json");

		java = manage.javaPackage("restless.examples").file("NonEmptyNonNegativeIntList");
		java.data().putResource("/java-example/NonEmptyNonNegativeIntList", "text/plain");
	}

	private void schemaSetup()
	{
		entitySetup();
		manage.entity(ENTITY_ID).putEntity(null, schema.url(), null);
	}

	private void javaSetup()
	{
		entitySetup();
		manage.entity(ENTITY_ID).putEntity(null, null, java.url());
	}

	@Test
	public void entity_canReadBack() throws Exception
	{
		entitySetup();
		manage.entity(ENTITY_ID).putEntity(null, schema.url(), java.url());

		final ApiEntity result = manage.entity(ENTITY_ID).getEntity();

		assertThat(result.javaUrl(), is(java.url()));
		assertThat(result.jsonSchemaUrl(), is(schema.url()));
		assertThat(result.discovered(), is(false));
	}

	@Test
	public void entity_isListed() throws Exception
	{
		entitySetup();

		assertThat(manage.listEntities().childResources().size(), is(0));

		final ManagementPathEntity entity = manage.entity(ENTITY_ID);
		entity.putEntity(null, schema.url(), java.url());

		final List<ListEntityItem> result = manage.listEntities().childResources();

		assertThat(result.size(), is(1));
		assertThat(result.get(0).entityId(), is(ENTITY_ID));
		assertThat(result.get(0).url(), is(entity.url()));
		assertFalse("Listed entity should not be marked as discovered", result.get(0).discovered());
	}

	@Test
	public void entity_withDiscovered_cannotStore() throws Exception
	{
		entitySetup();
		final ResponseType response1 = manage.entity("my-entity-1").putEntityResponseType(true, null, schema.url(),
				java.url());
		final ResponseType response2 = manage.entity("my-entity-2").putEntityResponseType(false, null, schema.url(),
				java.url());

		assertThat(response1, is(ResponseType.BAD_REQUEST));
		assertThat(response2, is(ResponseType.NO_CONTENT));
	}

	@Test
	public void nonexistentEntity_cannotFindEntity_and_cannotFindRootComponent() throws Exception
	{
		schemaSetup();

		assertThat(manage.entity("some-entity-that-is-not-there").getEntityResponseType(),
				is(ResponseType.NOT_FOUND));

		assertThat(manage.entity("some-entity-that-is-not-there").getComponentResponseType(ROOT),
				is(ResponseType.NOT_FOUND));
	}

	@Test
	public void entityWithNoHandlers_exists_but_cannotFindRootComponent() throws Exception
	{
		entitySetup();
		manage.entity(ENTITY_ID).putEntity(null, null, null);

		assertThat(manage.entity(ENTITY_ID).getEntityResponseType(),
				is(ResponseType.OK));

		assertThat(manage.entity(ENTITY_ID).getComponentResponseType(ROOT),
				is(ResponseType.NOT_FOUND));
	}

	@Test
	public void schemaEntity_canFindRootComponent() throws Exception
	{
		schemaSetup();

		final ApiComponent result = manage.entity(ENTITY_ID).getComponent(ROOT);

		assertTrue("Component should be root", result.jsonSchema().isRoot());
	}

	@Test
	public void schemaEntity_cannotFindNonsenseComponent() throws Exception
	{
		schemaSetup();

		assertThat(manage.entity(ENTITY_ID).getComponentResponseType(ROOT), is(ResponseType.OK));
		assertThat(manage.entity(ENTITY_ID).getComponentResponseType("asdf"), is(ResponseType.NOT_FOUND));
	}

	@Test
	public void schemaEntity_canFindTaggedComponent() throws Exception
	{
		schemaSetup();

		final ApiComponent result = manage.entity(ENTITY_ID).getComponent("el");

		assertFalse("Component should not be root", result.jsonSchema().isRoot());
	}

	@Test
	public void schemaEntity_canListComponents() throws Exception
	{
		schemaSetup();

		final ManagementPathEntity entity = manage.entity(ENTITY_ID);
		final List<ListComponentItem> list = entity.listComponents().childResources();
		final List<String> componentIds = Lists.transform(list, ListComponentItem::componentId);
		final List<String> urls = Lists.transform(list, ListComponentItem::url);

		assertThat(list.size(), is(2));
		assertThat(componentIds, containsInAnyOrder(".", "el"));
		assertThat(urls, hasItem(entity.urlOfComponent("el")));
	}

	@Test
	public void javaEntity_canFindRootComponent() throws Exception
	{
		javaSetup();

		final ApiComponent result = manage.entity(ENTITY_ID).getComponent(ROOT);

		assertTrue("Component should be root", result.java().isRoot());
	}

	@Test
	public void javaEntity_cannotFindNonsenseComponent() throws Exception
	{
		javaSetup();

		assertThat(manage.entity(ENTITY_ID).getComponentResponseType(ROOT), is(ResponseType.OK));
		assertThat(manage.entity(ENTITY_ID).getComponentResponseType("asdf"), is(ResponseType.NOT_FOUND));
	}
}
