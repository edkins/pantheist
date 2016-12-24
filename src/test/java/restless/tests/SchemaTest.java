package restless.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import restless.client.api.ResponseType;

public class SchemaTest extends BaseTest
{
	@Test
	public void schema_canReadItBack() throws Exception
	{
		manage.segment("my-binding").star().config().bindToFilesystem();
		manage.segment("my-binding").star().schema().putResource("/json-schema/coffee", "application/schema+json");

		final String data = manage.segment("my-binding").star().schema().getString("application/schema+json");

		JSONAssert.assertEquals(data, resource("/json-schema/coffee"), true);
	}

	@Test
	public void invalidSchema_rejected() throws Exception
	{
		manage.segment("my-binding").star().config().bindToFilesystem();
		final ResponseType responseType = manage.segment("my-binding").star().schema()
				.putResourceResponseType("/json-schema/invalid", "application/schema+json");

		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}

	@Test
	public void schema_validData_allowed() throws Exception
	{
		manage.segment("my-binding").star().config().bindToFilesystem();
		manage.segment("my-binding").star().schema().putResource("/json-schema/coffee", "application/schema+json");
		ResponseType responseType = manage.segment("my-binding").segment("my-file").data()
				.putResourceResponseType("/json-example/coffee-valid", "text/plain");
		assertEquals(ResponseType.NO_CONTENT, responseType);
	}

	@Test
	public void schema_invalidData_notAllowed() throws Exception
	{
		manage.segment("my-binding").star().config().bindToFilesystem();
		manage.segment("my-binding").star().schema().putResource("/json-schema/coffee", "application/schema+json");
		ResponseType responseType = manage.segment("my-binding").segment("my-file").data()
				.putResourceResponseType("/json-example/coffee-invalid", "text/plain");
		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}
}
