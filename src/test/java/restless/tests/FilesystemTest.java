package restless.tests;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import restless.api.management.model.ListConfigItem;
import restless.client.api.ManagementPathLocation;
import restless.client.api.ResponseType;

public class FilesystemTest extends BaseTest
{
	@Test
	public void filesystemFile_canReadItBack_throughManagementApi() throws Exception
	{
		mmain.location("/my-binding/").bindToFilesystem();
		manage.data("my-binding/my-file").putString("Contents of file");

		final String data = manage.data("my-binding/my-file").getString("text/plain");
		assertEquals("Contents of file", data);
	}

	@Test
	public void filesystemFile_dataExists_otherfile_dataDoesNotExist() throws Exception
	{
		mmain.location("/my-binding/").bindToFilesystem();
		manage.data("my-binding/my-file").putString("Contents of file");

		assertEquals(ResponseType.OK,
				manage.data("my-binding/my-file").getResponseType());
		assertEquals(ResponseType.NOT_FOUND,
				manage.data("my-binding/other-file").getResponseType());
	}

	@Test
	public void filesystemFile_isServed() throws Exception
	{
		mmain.location("/my-binding/").bindToFilesystem();
		manage.data("my-binding/my-file").putString("Contents of file");

		assertEquals("Contents of file", mainApi.withSegment("my-binding").withSegment("my-file").getTextPlain());
	}

	@Test
	public void resourceFile_isServed() throws Exception
	{
		final String contents = mainApi.withSegment("resources").withSegment("example-client.html")
				.getString("text/html");
		assertThat(contents, containsString("<html>"));
	}

	@Test
	public void externalFile_isServed() throws Exception
	{
		final File tempDir = mainRule.createTempDir();
		final File myfile = new File(tempDir, "myfile.txt");
		FileUtils.writeStringToFile(myfile, "Contents of my external file", StandardCharsets.UTF_8);
		mmain.location("/external-files/").bindToExternalFiles(tempDir.getAbsolutePath() + "/");

		final String contents = mainApi.withSegment("external-files").withSegment("myfile.txt").getTextPlain();
		assertThat(contents, is("Contents of my external file"));
	}

	@Test
	public void filesystemBinding_canDeleteBinding() throws Exception
	{
		final ManagementPathLocation configPoint = mmain.location("/my-binding/");
		configPoint.bindToFilesystem();
		assertTrue("Configuration point should exist", configPoint.exists());
		configPoint.delete();
		assertFalse("Configuration point should no longer exist", configPoint.exists());
	}

	@Test
	public void filesystemBinding_canListBindings() throws Exception
	{
		final int initialSize = mmain.listLocations().size();

		final ManagementPathLocation configPoint1 = mmain.location("/my-binding/");
		final ManagementPathLocation configPoint2 = mmain.location("/my-other-binding/");

		configPoint1.bindToFilesystem();
		configPoint2.bindToFilesystem();

		final List<ListConfigItem> list = mmain.listLocations();
		assertThat(list.size(), is(initialSize + 2));
		assertThat(list.get(initialSize + 0).url(), is(configPoint1.url()));
		assertThat(list.get(initialSize + 1).url(), is(configPoint2.url()));
	}
}
