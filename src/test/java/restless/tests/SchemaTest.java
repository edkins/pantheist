package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import restless.api.schema.model.ListSchemaItem;
import restless.client.api.ManagementDataSchema;
import restless.client.api.ResponseType;

public class SchemaTest extends BaseTest
{
	private ManagementDataSchema schema;

	@Before
	public void setup2()
	{
		schema = mainRule.actions().manage().jsonSchema("coffee");
	}

	@Test
	public void schema_canReadItBack() throws Exception
	{
		schema.putResource("/json-schema/coffee", "application/schema+json");

		final String data = schema.getString("application/schema+json");

		JSONAssert.assertEquals(data, resource("/json-schema/coffee"), true);
	}

	@Test
	public void schema_canList() throws Exception
	{
		schema.putResource("/json-schema/coffee", "application/schema+json");

		final List<ListSchemaItem> list = manage.listJsonSchemas().childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(schema.url()));
	}

	@Test
	public void invalidSchema_rejected() throws Exception
	{
		final ResponseType responseType = schema
				.putResourceResponseType("/json-schema/invalid", "application/schema+json");

		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}

	@Test
	public void schema_validData_allowed() throws Exception
	{
		schema.putResource("/json-schema/coffee", "application/schema+json");
		final ResponseType responseType = schema.validate(resource("/json-example/coffee-valid"), "application/json");
		assertEquals(ResponseType.NO_CONTENT, responseType);
	}

	@Test
	public void schema_invalidData_notAllowed() throws Exception
	{
		schema.putResource("/json-schema/coffee", "application/schema+json");
		final ResponseType responseType = schema.validate(resource("/json-example/coffee-invalid"), "application/json");
		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}
}
