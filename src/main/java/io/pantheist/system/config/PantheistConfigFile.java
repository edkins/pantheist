package io.pantheist.system.config;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * This looks almost identical to PantheistConfig. It's only here
 * to hide the exact contents of the config file from the rest of the program.
 *
 * Also values can be missing, and will be supplied with defaults when this
 * gets transformed into a PantheistConfig.
 */
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(as = PantheistConfigFileImpl.class)
interface PantheistConfigFile
{
	@Nullable
	@JsonProperty("internalPort")
	Integer internalPort();

	@Nullable
	@JsonProperty("nginxPort")
	Integer nginxPort();

	@Nullable
	@JsonProperty("postgresPort")
	Integer postgresPort();

	@Nullable
	@JsonProperty("dataDir")
	String dataDir();

	/**
	 * Must be inside dataDir.
	 */
	@Nullable
	@JsonProperty("systemDir")
	String systemDir();

	/**
	 * Must be inside dataDir.
	 */
	@Nullable
	@JsonProperty("srvDir")
	String srvDir();

	String nginxExecutable();

}
