package restless.testhelpers.app;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.io.Files;

import restless.testhelpers.session.TestSession;

public class TempDirRule implements TestRule
{
	private final TestSession session;

	private TempDirRule(final TestSession session)
	{
		this.session = checkNotNull(session);
	}

	public static TestRule forTest(final TestSession session)
	{
		return new TempDirRule(session);
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return new Statement() {

			@Override
			public void evaluate() throws Throwable
			{
				final File tempDir = Files.createTempDir();
				try
				{
					session.supplyDataDir(tempDir);
					base.evaluate();
				}
				finally
				{
					FileUtils.deleteDirectory(tempDir);
				}
			}
		};
	}

}
