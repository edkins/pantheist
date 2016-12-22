package restless.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import restless.client.api.ManagementPath;
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

	@Before
	public void setup()
	{
		manage = sessionRule.actions().manage();
	}

	@Test
	public void filesystemFile_canReadItBack_throughManagementApi() throws Exception
	{
		manage.segment("my-binding").config().bindToFilesystem("my-fs-binding");
		manage.segment("my-binding").segment("my-file").data().putString("Contents of file");

		assertEquals("Contents of file", manage.segment("my-binding").segment("my-file").data().getString());
	}
}
