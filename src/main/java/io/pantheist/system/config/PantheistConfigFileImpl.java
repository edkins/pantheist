package io.pantheist.system.config;

import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonProperty;

final class PantheistConfigFileImpl implements PantheistConfigFile
{
	private final Integer internalPort;
	private final Integer nginxPort;
	private final String dataDir;
	private final String systemDir;
	private final String srvDir;
	private final String nginxExecutable;

	@Inject
	private PantheistConfigFileImpl(
			@JsonProperty("internalPort") final Integer internalPort,
			@JsonProperty("nginxPort") final Integer nginxPort,
			@JsonProperty("dataDir") final String dataDir,
			@JsonProperty("systemDir") final String systemDir,
			@JsonProperty("srvDir") final String srvDir,
			@JsonProperty("nginxExecutable") final String nginxExecutable)
	{
		this.internalPort = internalPort;
		this.nginxPort = nginxPort;
		this.dataDir = dataDir;
		this.systemDir = systemDir;
		this.srvDir = srvDir;
		this.nginxExecutable = nginxExecutable;
	}

	@Override
	public Integer internalPort()
	{
		return internalPort;
	}

	@Override
	public Integer nginxPort()
	{
		return nginxPort;
	}

	@Override
	public String dataDir()
	{
		return dataDir;
	}

	@Override
	public String systemDir()
	{
		return systemDir;
	}

	@Override
	public String srvDir()
	{
		return srvDir;
	}

	@Override
	public String nginxExecutable()
	{
		return nginxExecutable;
	}

}
