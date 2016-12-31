package io.pantheist.system.config;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;

public class SystemConfigModule extends PrivateModule
{
	private final String[] args;

	public SystemConfigModule(final String[] args)
	{
		this.args = args;
	}

	@Override
	protected void configure()
	{
		expose(PantheistConfig.class);
		bind(PantheistConfig.class).to(PantheistConfigImpl.class).in(Scopes.SINGLETON);

		bind(String[].class).annotatedWith(CmdLineArgumentArray.class).toInstance(args);
		bind(CmdLineArguments.class).to(CmdLineArgumentsImpl.class).in(Scopes.SINGLETON);
		bind(ArgumentProcessor.class).to(ArgumentProcessorImpl.class).in(Scopes.SINGLETON);
	}
}
