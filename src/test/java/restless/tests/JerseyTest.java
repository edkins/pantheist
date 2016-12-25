package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import restless.client.api.ManagementConfigPoint;
import restless.client.api.ResponseType;

public class JerseyTest extends BaseTest
{
	@Test
	public void jerseyResource_canPutSomewhere_andReadItBack() throws Exception
	{
		final ManagementConfigPoint configPoint = manage.config().create("my-binding");
		configPoint.jerseyFile()
				.putResource("/jersey-resource/resource", "text/plain");

		final String data = configPoint.jerseyFile().getString("text/plain");

		assertThat(data, is(resource("/jersey-resource/resource")));
	}

	@Test
	public void invalidJava_cannotStore() throws Exception
	{
		ManagementConfigPoint configPoint = manage.config().create("my-binding");
		final ResponseType responseType = configPoint.jerseyFile()
				.putResourceResponseType("/jersey-resource/java-syntax-error", "text/plain");

		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}

}
