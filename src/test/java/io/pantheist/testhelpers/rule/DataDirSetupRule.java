package io.pantheist.testhelpers.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.collect.ImmutableSet;

import io.pantheist.handler.kind.model.Kind;
import io.pantheist.testhelpers.classrule.TestSession;

final class DataDirSetupRule implements TestRule
{
	private static final Logger LOGGER = LogManager.getLogger(DataDirSetupRule.class);
	private final TestSession session;

	private DataDirSetupRule(final TestSession session)
	{
		this.session = checkNotNull(session);
	}

	public static DataDirSetupRule forTest(final TestSession session)
	{
		return new DataDirSetupRule(session);
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

	private void copyBuiltInKinds(final File sourceDir, final File targetDir) throws IOException
	{
		for (final String name : sourceDir.list())
		{
			final File source = new File(sourceDir, name);
			final File target = new File(targetDir, name);
			if (source.isFile())
			{
				final Kind kind = session.objectMapper().readValue(source, Kind.class);
				if (kind.partOfSystem())
				{
					final String oldText = FileUtils.readFileToString(source, StandardCharsets.UTF_8);

					final String text = oldText.replace("127.0.0.1:3142", "127.0.0.1:" + session.nginxPort());

					FileUtils.writeStringToFile(target, text, StandardCharsets.UTF_8);
				}
			}
		}
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
			cleanAllExcept(system, "database", "nginx.pid", "nginx.conf");
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

		deanonymizeNginxConf(
				new File(source, "system/nginx-anon.conf"),
				new File(target, "system/nginx.conf"),
				system.getAbsolutePath(),
				srv.getAbsolutePath(),
				target.getAbsolutePath(),
				"127.0.0.1:" + session.nginxPort(),
				"127.0.0.1:" + session.internalPort());

		copyBuiltInKinds(new File(source, "project/kind"), kind);
	}

	private void deanonymizeNginxConf(
			final File nginxAnonConf,
			final File nginxConf,
			final String hiddenText0,
			final String hiddenText1,
			final String hiddenText2,
			final String hiddenText3,
			final String hiddenText4) throws IOException
	{
		final String replacementText0 = "${SYSTEM_DIR}";
		final String replacementText1 = "${SRV_DIR}";
		final String replacementText2 = "${DATA_DIR}";
		final String replacementText3 = "127.0.0.1:${MAIN_PORT}";
		final String replacementText4 = "127.0.0.1:${MANAGEMENT_PORT}";

		final String text = FileUtils
				.readFileToString(nginxAnonConf, StandardCharsets.UTF_8)
				.replace(replacementText0, hiddenText0)
				.replace(replacementText1, hiddenText1)
				.replace(replacementText2, hiddenText2)
				.replace(replacementText3, hiddenText3)
				.replace(replacementText4, hiddenText4);

		FileUtils.write(nginxConf, text, StandardCharsets.UTF_8);
	}

}
