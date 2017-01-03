package io.pantheist.system.config;

import com.fasterxml.jackson.annotation.JsonProperty;

final class PantheistConfigFileImpl implements PantheistConfigFile
{
	private final Integer internalPort;
	private final Integer nginxPort;
	private final Integer postgresPort;
	private final String dataDir;
	private final String systemDir;
	private final String srvDir;
	private final String projectDir;
	private final String nginxExecutable;

	private PantheistConfigFileImpl(
			@JsonProperty("internalPort") final Integer internalPort,
			@JsonProperty("nginxPort") final Integer nginxPort,
			@JsonProperty("postgresPort") final Integer postgresPort,
			@JsonProperty("dataDir") final String dataDir,
			@JsonProperty("systemDir") final String systemDir,
			@JsonProperty("srvDir") final String srvDir,
			@JsonProperty("projectDir") final String projectDir,
			@JsonProperty("nginxExecutable") final String nginxExecutable)
	{
		this.internalPort = internalPort;
		this.nginxPort = nginxPort;
		this.postgresPort = postgresPort;
		this.dataDir = dataDir;
		this.systemDir = systemDir;
		this.srvDir = srvDir;
		this.projectDir = projectDir;
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
	public String projectDir()
	{
		return projectDir;
	}

	@Override
	public String nginxExecutable()
	{
		return nginxExecutable;
	}

	@Override
	public Integer postgresPort()
	{
		return postgresPort;
	}

}
