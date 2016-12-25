package restless.tests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import restless.client.api.ResponseType;

public class FilesystemTest extends BaseTest
{
	@Test
	public void filesystemFile_canReadItBack_throughManagementApi() throws Exception
	{
		manage.config().create("my-binding").bindToFilesystem();
		manage.data("my-binding/my-file").putString("Contents of file");

		final String data = manage.data("my-binding/my-file").getString("text/plain");
		assertEquals("Contents of file", data);
	}

	@Test
	public void filesystemFile_dataExists_otherfile_dataDoesNotExist() throws Exception
	{
		manage.config().create("my-binding").bindToFilesystem();
		manage.data("my-binding/my-file").putString("Contents of file");

		assertEquals(ResponseType.OK,
				manage.data("my-binding/my-file").getResponseType());
		assertEquals(ResponseType.NOT_FOUND,
				manage.data("my-binding/other-file").getResponseType());
	}

	@Test
	public void filesystemFile_isServed() throws Exception
	{
		manage.config().create("my-binding").bindToFilesystem();
		manage.data("my-binding/my-file").putString("Contents of file");

		assertEquals("Contents of file", mainApi.withSegment("my-binding").withSegment("my-file").getTextPlain());
	}

	@Test
	public void resourceFile_isServed() throws Exception
	{
		manage.config().create("my-resources").bindToResourceFiles("");

		final String contents = mainApi.withSegment("my-resources").withSegment("example-resource.txt").getTextPlain();
		assertThat(contents, is(resource("/resource-files/example-resource.txt")));
	}
}
