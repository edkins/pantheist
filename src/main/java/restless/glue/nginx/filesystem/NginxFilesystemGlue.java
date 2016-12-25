package restless.glue.nginx.filesystem;

import restless.handler.nginx.model.NginxConfig;

public interface NginxFilesystemGlue
{
	/**
	 * If nginx needs to serve something:
	 * - if it's not running then start it
	 * - if it's running then restart it
	 * Otherwise:
	 * - if it's running then stop it
	 */
	void startStopOrRestart();

	NginxConfig nginxConf();
}
