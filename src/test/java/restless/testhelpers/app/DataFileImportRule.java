package restless.testhelpers.app;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import restless.testhelpers.session.TestSession;

public class DataFileImportRule implements TestRule
{
	private static final Logger LOGGER = LogManager.getLogger(TempDirRule.class);
	private final TestSession session;

	private DataFileImportRule(final TestSession session)
	{
		this.session = checkNotNull(session);
	}

	public static DataFileImportRule forTest(final TestSession session)
	{
		return new DataFileImportRule(session);
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return new Statement() {

			@Override
			public void evaluate() throws Throwable
			{
				final File target = session.dataDir();
				new File(target, "srv").mkdir();
				new File(target, "system").mkdir();

				final File source = session.originalDataDir();
				LOGGER.info("Copying stuff from {} to {}", source.getAbsolutePath(), target.getAbsolutePath());
				FileUtils.copyDirectory(new File(source, "srv/resources"), new File(target, "srv/resources"));
				FileUtils.copyFile(new File(source, "system/bindings"), new File(target, "system/bindings"));
				base.evaluate();
			}
		};
	}

}
