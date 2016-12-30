package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import restless.api.java.model.ListFileItem;
import restless.client.api.ManagementPathJavaFile;
import restless.client.api.ResponseType;

public class JavaTest extends BaseTest
{
	private static final String JAVA_PKG = "restless.examples";
	private static final String JAVA_EMPTY_CLASS_RES = "/java-example/EmptyClass";
	private static final String JAVA_EMPTY_CLASS_NAME = "EmptyClass";

	@Test
	public void java_canPutSomewhere_andReadItBack() throws Exception
	{
		manage.javaPackage(JAVA_PKG)
				.file("ExampleJerseyResource")
				.data()
				.putResource("/jersey-resource/resource", "text/plain");

		final String data = manage
				.javaPackage(JAVA_PKG)
				.file("ExampleJerseyResource")
				.data()
				.getString("text/plain");

		assertThat(data, is(resource("/jersey-resource/resource")));
	}

	@Test
	public void invalidJava_cannotStore() throws Exception
	{
		final ResponseType responseType = manage.javaPackage(JAVA_PKG)
				.file("ExampleJerseyResource")
				.data()
				.putResourceResponseType("/jersey-resource/java-syntax-error", "text/plain");

		assertEquals(ResponseType.BAD_REQUEST, responseType);
	}

	@Test
	public void java_canList() throws Exception
	{
		final ManagementPathJavaFile file = manage.javaPackage(JAVA_PKG).file(JAVA_EMPTY_CLASS_NAME);
		file.data().putResource(JAVA_EMPTY_CLASS_RES, "text/plain");

		final List<ListFileItem> list = manage.javaPackage(JAVA_PKG).listFiles().childResources();

		assertThat(list.size(), is(1));
		assertThat(list.get(0).url(), is(file.url()));
	}
}
