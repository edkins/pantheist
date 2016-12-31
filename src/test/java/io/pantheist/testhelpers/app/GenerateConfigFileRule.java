package io.pantheist.testhelpers.app;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.pantheist.testhelpers.session.TestSession;

public class GenerateConfigFileRule implements TestRule
{
	private final TestSession session;

	private GenerateConfigFileRule(final TestSession session)
	{
		this.session = checkNotNull(session);
	}

	public static TestRule forTest(final TestSession session)
	{
		return new GenerateConfigFileRule(session);
	}

	@Override
	public Statement apply(final Statement base, final Description description)
	{
		return new Statement() {

			@Override
			public void evaluate() throws Throwable
			{
				final File configFile = new File(session.dataDir(), "pantheist.conf");

				final Map<String, Object> map = new HashMap<>();
				map.put("dataDir", session.dataDir().getAbsolutePath());
				map.put("nginxPort", session.nginxPort());
				map.put("internalPort", session.internalPort());

				session.objectMapper().writeValue(configFile, map);
				session.supplyConfigFile(configFile);
				base.evaluate();
			}
		};
	}

}
