package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import restless.client.api.ResponseType;

public class JerseyTest extends BaseTest
{
	@Test
	public void jerseyResource_canPutSomewhere_andReadItBack() throws Exception
	{
		manage.segment("my-binding").jerseyFile()
				.putResource("/jersey-resource/resource", "text/plain");

		final String data = manage.segment("my-binding").jerseyFile().getString("text/plain");

		assertThat(data, is(resource("/jersey-resource/resource")));
	}

	@Test
	public void invalidJava_cannotStore() throws Exception
	{
		final ResponseType responseType = manage.segment("my-binding").jerseyFile()
				.putResourceResponseType("/jersey-resource/java-syntax-error", "text/plain");

		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}

}
