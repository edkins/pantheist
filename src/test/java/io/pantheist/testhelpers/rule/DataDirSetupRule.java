package io.pantheist.testhelpers.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.collect.ImmutableSet;

import io.pantheist.testhelpers.classrule.TestSession;

final class DataDirSetupRule implements TestRule
{
	private static final Logger LOGGER = LogManager.getLogger(DataDirSetupRule.class);
	private final TestSession session;
	private final boolean cleanNginxConf;

	private DataDirSetupRule(final TestSession session, final boolean cleanNginxConf)
	{
		this.session = checkNotNull(session);
		this.cleanNginxConf = cleanNginxConf;
	}

	public static DataDirSetupRule forTest(final TestSession session, final boolean cleanNginxConf)
	{
		return new DataDirSetupRule(session, cleanNginxConf);
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return new Statement() {

			@Override
			public void evaluate() throws Throwable
			{
				cleanOldFiles();
				copyFiles();

				base.evaluate();
			}
		};
	}

	/**
	 * Delete any files left over from the last test.
	 *
	 * We don't create a completely fresh directory each time because of the postgres database,
	 * which is slow to initialize.
	 */
	private void cleanOldFiles() throws IOException
	{
		final File root = session.dataDir();
		final File system = new File(root, "system");

		if (root.isDirectory())
		{
			if (cleanNginxConf)
			{
				cleanAllExcept(system, "database", "nginx.pid");
			}
			else
			{
				cleanAllExcept(system, "database", "nginx.pid", "nginx.conf");
			}
			cleanAllExcept(system, "database", "nginx.pid");
			cleanAllExcept(root, "system", "pantheist.conf");
		}
		else
		{
			root.mkdir();
		}
	}

	private void cleanAllExcept(final File dir, final String... exceptions) throws IOException
	{
		final Set<String> exceptionSet = ImmutableSet.copyOf(exceptions);
		if (dir.isDirectory())
		{
			for (final File file : dir.listFiles())
			{
				if (!exceptionSet.contains(file.getName()))
				{
					if (file.isDirectory())
					{
						FileUtils.deleteDirectory(file);
					}
					else
					{
						file.delete();
					}
				}
			}
		}
	}

	private void copyFiles() throws IOException
	{
		final File target = session.dataDir();
		final File srv = new File(target, "srv");
		final File system = new File(target, "system");
		final File kind = new File(target, "project/kind");
		srv.mkdir();
		system.mkdir();
		kind.mkdirs();

		final File source = session.originalDataDir();
		LOGGER.info("Copying stuff from {} to {}", source.getAbsolutePath(), target.getAbsolutePath());
		FileUtils.copyDirectory(new File(source, "srv/resources"), new File(target, "srv/resources"));
	}

}
