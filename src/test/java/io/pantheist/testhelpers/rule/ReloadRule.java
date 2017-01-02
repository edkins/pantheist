package io.pantheist.testhelpers.rule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.pantheist.testhelpers.classrule.TestSession;

final class ReloadRule implements TestRule
{
	private static final Logger LOGGER = LogManager.getLogger(ReloadRule.class);
	private final TestSession session;

	public ReloadRule(final TestSession session)
	{
		this.session = checkNotNull(session);
	}

	public static TestRule forTest(final TestSession session)
	{
		return new ReloadRule(session);
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return new Statement() {

			@Override
			public void evaluate() throws Throwable
			{
				if (session.ensureStarted())
				{
					waitForServer();
					session.client().regenerateDb();
				}
				else
				{
					waitForServer();
				}
				base.evaluate();
			}
		};
	}

	private void waitForServer() throws InterruptedException, ServerNeverAppearedException, URISyntaxException
	{
		for (int i = 0;; i++)
		{
			try
			{
				final URL url = UriBuilder.fromUri(session.mainUrl().toURI()).path("system/ping").build().toURL();
				LOGGER.info("Pinging {}", url);
				url.getContent();
				return;
			}
			catch (final IOException e)
			{
				LOGGER.trace(e);
				Thread.sleep(100);
				if (i > 20)
				{
					throw new ServerNeverAppearedException(e);
				}
			}
		}
	}

}
