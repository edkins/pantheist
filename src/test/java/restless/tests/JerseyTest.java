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
		manage.javaPackage("restless.examples").file("ExampleJerseyResource")
				.putResource("/jersey-resource/resource", "text/plain");

		final String data = manage.javaPackage("restless.examples").file("ExampleJerseyResource")
				.getString("text/plain");

		assertThat(data, is(resource("/jersey-resource/resource")));
	}

	@Test
	public void invalidJava_cannotStore() throws Exception
	{
		final ResponseType responseType = manage.javaPackage("restless.examples").file("ExampleJerseyResource")
				.putResourceResponseType("/jersey-resource/java-syntax-error", "text/plain");

		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}

}
