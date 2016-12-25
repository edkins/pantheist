package restless.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import restless.client.api.ManagementConfigPoint;
import restless.client.api.ResponseType;

public class SchemaTest extends BaseTest
{
	@Test
	public void schema_canReadItBack() throws Exception
	{
		final ManagementConfigPoint configPoint = manage.config().create("my-binding");
		configPoint.schema().putResource("/json-schema/coffee", "application/schema+json");

		final String data = configPoint.schema().getString("application/schema+json");

		JSONAssert.assertEquals(data, resource("/json-schema/coffee"), true);
	}

	@Test
	public void invalidSchema_rejected() throws Exception
	{
		final ManagementConfigPoint configPoint = manage.config().create("my-binding");
		configPoint.bindToFilesystem();
		final ResponseType responseType = configPoint.schema()
				.putResourceResponseType("/json-schema/invalid", "application/schema+json");

		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}

	@Test
	public void schema_validData_allowed() throws Exception
	{
		final ManagementConfigPoint configPoint = manage.config().create("my-binding");
		configPoint.bindToFilesystem();
		configPoint.schema().putResource("/json-schema/coffee", "application/schema+json");
		final ResponseType responseType = manage.data("my-binding/my-file")
				.putResourceResponseType("/json-example/coffee-valid", "text/plain");
		assertEquals(ResponseType.NO_CONTENT, responseType);
	}

	@Test
	public void schema_invalidData_notAllowed() throws Exception
	{
		final ManagementConfigPoint configPoint = manage.config().create("my-binding");
		configPoint.bindToFilesystem();
		configPoint.schema().putResource("/json-schema/coffee", "application/schema+json");
		final ResponseType responseType = manage.data("my-binding/my-file")
				.putResourceResponseType("/json-example/coffee-invalid", "text/plain");
		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}
}
