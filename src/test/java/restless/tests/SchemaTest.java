package restless.tests;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

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
}
