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
		manage.segment("my-binding").star().config().bindToFilesystem();
		manage.segment("my-binding").segment("my-file").data().putString("Contents of file");

		assertEquals("Contents of file",
				manage.segment("my-binding").segment("my-file").data().getString("text/plain"));
	}

	@Test
	public void filesystemFile_dataExists_otherfile_dataDoesNotExist() throws Exception
	{
		manage.segment("my-binding").star().config().bindToFilesystem();
		manage.segment("my-binding").segment("my-file").data().putString("Contents of file");

		assertEquals(ResponseType.OK,
				manage.segment("my-binding").segment("my-file").data().getResponseType());
		assertEquals(ResponseType.NOT_FOUND,
				manage.segment("my-binding").segment("other-file").data().getResponseType());
	}

	@Test
	public void filesystemFile_isServed() throws Exception
	{
		manage.segment("my-binding").star().config().bindToFilesystem();
		manage.segment("my-binding").segment("my-file").data().putString("Contents of file");

		assertEquals("Contents of file", mainApi.withSegment("my-binding").withSegment("my-file").getTextPlain());
	}

	@Test
	public void resourceFile_isServed() throws Exception
	{
		manage.segment("my-resources").star().config().bindToResourceFiles("");

		final String contents = mainApi.withSegment("my-resources").withSegment("example-resource.txt").getTextPlain();
		assertThat(contents, is(resource("/resource-files/example-resource.txt")));
	}
}
