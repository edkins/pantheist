package restless.tests;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import restless.api.management.model.ApiComponent;
import restless.api.management.model.ApiEntity;
import restless.client.api.ManagementData;
import restless.client.api.ManagementDataSchema;
import restless.client.api.ResponseType;

public class EntityTest extends BaseTest
{
	private ManagementDataSchema schema;
	private ManagementData java;

	private void entitySetup()
	{
		schema = mainRule.actions().manage().jsonSchema("my_schema");
		schema.putResource("/json-schema/nonempty_nonnegative_int_list", "application/schema+json");

		java = manage.javaPackage("restless.examples").file("ExampleJerseyResource");
		java.putResource("/jersey-resource/resource", "text/plain");
	}

	private void schemaSetup()
	{
		entitySetup();
		manage.entity("my-entity").putEntity(schema.url(), null);
	}

	@Test
	public void entity_canReadBack() throws Exception
	{
		entitySetup();
		manage.entity("my-entity").putEntity(schema.url(), java.url());

		final ApiEntity result = manage.entity("my-entity").getEntity();

		assertThat(result.javaUrl(), is(java.url()));
		assertThat(result.jsonSchemaUrl(), is(schema.url()));
	}

	@Test
	public void schemaEntity_canFindRootComponent() throws Exception
	{
		schemaSetup();

		final ApiComponent result = manage.entity("my-entity").getComponent("root");

		assertTrue("Component should be root", result.jsonSchema().isRoot());
	}

	@Test
	public void schemaEntity_cannotFindNonsenseComponent() throws Exception
	{
		schemaSetup();

		assertThat(manage.entity("my-entity").getComponentResponseType("root"), is(ResponseType.OK));
		assertThat(manage.entity("my-entity").getComponentResponseType("asdf"), is(ResponseType.NOT_FOUND));
	}

	@Test
	public void schemaEntity_canFindTaggedComponent() throws Exception
	{
		schemaSetup();

		final ApiComponent result = manage.entity("my-entity").getComponent("el");

		assertFalse("Component should not be root", result.jsonSchema().isRoot());
	}

	@Test
	public void schemaEntity_canListComponents() throws Exception
	{
		schemaSetup();

		final List<String> componentIds = manage.entity("my-entity").listComponentIds();

		assertThat(componentIds, contains("root", "el"));
	}
}
