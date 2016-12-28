package restless.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import restless.client.api.ManagementDataSchema;
import restless.client.api.ResponseType;

public class SchemaTest extends BaseTest
{
	private ManagementDataSchema schema;

	@Override
	@Before
	public void setup()
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
