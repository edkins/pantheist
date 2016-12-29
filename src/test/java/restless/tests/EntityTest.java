package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import restless.api.management.model.ApiEntity;
import restless.client.api.ManagementData;
import restless.client.api.ManagementDataSchema;

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

	@Test
	public void entity_canReadBack() throws Exception
	{
		entitySetup();
		manage.entity("my-entity").putEntity(schema.url(), java.url());

		final ApiEntity result = manage.entity("my-entity").getEntity();

		assertThat(result.javaUrl(), is(java.url()));
		assertThat(result.jsonSchemaUrl(), is(schema.url()));
	}
}
