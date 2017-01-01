package io.pantheist.testhelpers.app;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.pantheist.handler.kind.model.Kind;
import io.pantheist.testhelpers.session.TestSession;

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
				final File srv = new File(target, "srv");
				final File system = new File(target, "system");
				final File kind = new File(target, "system/kind");
				srv.mkdir();
				system.mkdir();
				kind.mkdir();

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

				copyBuiltInKinds(new File(source, "system/kind"), kind);

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
					FileUtils.copyFile(source, target);
				}
			}
		}
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
