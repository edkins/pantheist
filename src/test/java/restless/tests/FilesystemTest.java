package restless.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import restless.client.api.ManagementPath;
import restless.client.api.ResponseType;
import restless.client.impl.TargetWrapper;
import restless.testhelpers.selenium.ApiRule;
import restless.testhelpers.selenium.Interaction;
import restless.testhelpers.session.MainRule;

public class FilesystemTest
{
	@ClassRule
	public static final ApiRule apiRule = Interaction.api();

	@Rule
	public final MainRule sessionRule = MainRule.forNewTest(apiRule);

	private ManagementPath manage;

	private TargetWrapper mainApi;

	@Before
	public void setup()
	{
		manage = sessionRule.actions().manage();
		mainApi = sessionRule.actions().main();
	}

	@Test
	public void filesystemFile_canReadItBack_throughManagementApi() throws Exception
	{
		manage.segment("my-binding").star().config().bindToFilesystem();
		manage.segment("my-binding").segment("my-file").data().putString("Contents of file");

		assertEquals("Contents of file", manage.segment("my-binding").segment("my-file").data().getString());
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
}
